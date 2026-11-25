# 实验四 · 本地排行榜与 SQLite

## 任务概述

从主菜单打开本地排行榜，可以查看简单、普通和困难三份榜单。用户可以在结算时记录本局得分，也可以用「添加记录」录入一条成绩。榜单按分数从高到低排列，第一名高亮，每行都可以确认后删除。关闭再打开应用，已保存的记录依然可读。

## 关键概念

- **SQLite 数据库**。Android 自带的关系型数据库。可以把这里的成绩表想成一张表格。每行是一条成绩，每列存一种信息，例如姓名、难度或分数。应用把数据库保存在自己的 `databases/rank.db` 中。
- **DAO（数据访问对象）**。榜单通过 Java 接口 `RankDao` 请求查询、保存或删除，由 `SQLiteRankDao` 实际操作数据库。这沿用了桌面版把页面与读写操作分开的做法。
- **ListView 和 Adapter**。ListView 提供一个能滚动的列表容器。Adapter（适配器）把一条成绩填到一行控件里，也负责把滚动出屏幕的行交给新的成绩复用。
- **后台任务**。数据库读写可能需要等待。页面把读写放到后台的单线程队列，取到结果后才回到 Android 主线程更新列表。

## 实现概览

假设你在「普通」难度打了 70 分。在结算点「记录成绩」时，`GameActivity` 把难度、本局的 70 分和局编号放入 Intent。排行榜页打开弹窗，让你补上名字。确认后，后台队列通过 `RankDao` 保存成绩并重新查出普通榜。最后，主线程用 `RankAdapter` 展示最新榜单。

下面依次追踪这条成绩。代码块节选自当前源码，可按链接打开完整文件。

## 关键代码

### 1. 录入时怎样检查姓名和分数

`RankEntryDialog.submit()` 在点「保存」时调用。结算入口使用已有的本局分数，手动录入入口才读取分数输入框。 [RankEntryDialog.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/rank/RankEntryDialog.java)

```java
private void submit(BiConsumer<String, Integer> onSave) {
    String name = nameInput.getText().toString().trim();
    if (name.isEmpty()) {
        nameInput.setError(activity.getString(R.string.enter_player_name));
        nameInput.requestFocus();
        return;
    }

    int score;
    try {
        score = roundScore >= 0 ? roundScore : Integer.parseInt(scoreInput.getText().toString());
        if (score < 0) {
            throw new NumberFormatException();
        }
    } catch (NumberFormatException exception) {
        scoreInput.setError(activity.getString(R.string.enter_valid_score));
        scoreInput.requestFocus();
        return;
    }
    onSave.accept(name, score);
}
```

1. `getText()` 读取输入框，`toString()` 转成 Java 字符串，`trim()` 去掉首尾空白。如果只录入了空格，去掉后就是空字符串，代码显示提示、把输入焦点移回姓名框，然后用 `return` 提前结束。
2. `条件 ? 值一 : 值二` 根据条件选择一个值。结算带来的 `roundScore` 是非负数，包括 0，所以直接使用它。手动录入时，`roundScore` 用 -1 表示没有本局分数，这时执行 `Integer.parseInt()`，把输入的文字转成整数。留空、字母、超出整数范围或负分都会进入异常处理并提示重输。
3. `BiConsumer<String, Integer>` 表示一个接收两个参数的动作。合法时，`onSave.accept(name, score)` 把姓名和分数传给页面提供的保存动作。从弹窗看，到这里就完成了输入检查。

### 2. DAO 接口为何单独定义

下面是排行榜调用的三个方法。桌面版用文件存放成绩，Android 版把同样的业务动作交给 SQLite 实现。 [RankDao.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/rank/RankDao.java)

```java
public interface RankDao {
    List<RankEntry> list(Difficulty difficulty);

    void save(Difficulty difficulty, String roundId, String name, int score, long playedAt);

    void delete(long id);
}
```

- `interface` 约定方法名称、参数和返回值。比如页面知道只要传一个 `Difficulty`，`list()` 就会返回该难度的一组成绩。
- `List<RankEntry>` 是泛型列表，尖括号里的 `RankEntry` 规定每个元素都是一条成绩。`RankEntry` 用 Java record 表示一行，保存数据库 ID、姓名、分数和时间。 [RankEntry.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/rank/RankEntry.java)
- `void` 表示方法完成一次操作，没有要回给调用者的值。页面保存或删除后，会再调用 `list()` 获取新的榜单。

### 3. SQLite 如何保存和查询一条成绩

`SQLiteRankDao` 继承 `SQLiteOpenHelper`。首次打开数据库时，Android 调用它的 `onCreate()` 建表。表里为每行生成 `_id`，并要求 `round_id` 唯一。前者标识数据库行，后者标识一场对局。 [SQLiteRankDao.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/rank/SQLiteRankDao.java)

```java
public void save(Difficulty difficulty, String roundId, String name, int score, long playedAt) {
    // A restored result can retry the same round without creating a second record.
    getWritableDatabase().execSQL("""
            INSERT INTO rank_entries (difficulty, round_id, name, score, played_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (round_id) DO NOTHING
            """, new Object[]{difficulty.name(), roundId, name, score, playedAt});
}
```

- 三个引号是 Java 的文本块，适合写多行 SQL。SQL 是向数据库发出指令的语言，这里的 `INSERT INTO` 表示向成绩表插入一行。
- `?` 是占位符，后面的参数数组按顺序填充。普通难度对应 `NORMAL`，姓名和 70 分各占一列。姓名以值的形式交给 SQLite，即使名字里有引号，也无需手动改写 SQL。
- `ON CONFLICT (round_id) DO NOTHING` 表示相同的局编号再次提交时保留已有成绩。例如系统恢复结算页面后重试保存，同一局仍然只有一条记录。

保存后，页面按下面的方法查出当前榜。 [SQLiteRankDao.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/rank/SQLiteRankDao.java)

```java
public List<RankEntry> list(Difficulty difficulty) {
    List<RankEntry> entries = new ArrayList<>();
    String[] columns = {"_id", "name", "score", "played_at"};
    String[] filter = {difficulty.name()};
    try (Cursor cursor = getReadableDatabase().query(TABLE, columns, "difficulty = ?", filter,
            null, null, "score DESC, played_at ASC, _id ASC")) {
        while (cursor.moveToNext()) {
            entries.add(new RankEntry(cursor.getLong(0), cursor.getString(1), cursor.getInt(2),
                    cursor.getLong(3)));
        }
    }
    return entries;
}
```

1. `String[]` 表示字符串数组。`columns` 指定要读的四列，`filter` 提供 `difficulty = ?` 的参数。例如当前是普通榜，数据库只返回 `NORMAL` 的记录。
2. `DESC` 指降序，`ASC` 指升序。排序顺序是高分在前，同分时早提交的记录在前，连时间也相同时则按行 ID 排序。这样两条 70 分的记录每次查询都有稳定顺序。
3. `Cursor` 是用来逐行读取查询结果的对象。`moveToNext()` 走到下一行并告诉代码是否还有记录。列下标从 0 开始，所以 `getLong(0)` 读出行 ID，`getString(1)` 读出姓名。每读一行就创建一个 `RankEntry` 放进列表。Java 的 `try (...)` 会在退出时自动关闭游标，即使途中发生异常也会尝试关闭。

### 4. 查询结果如何回到主线程

`RankActivity.runOnDatabase()` 接受要执行的动作，以及成功和失败时各自要做的事。 [RankActivity.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/RankActivity.java)

```java
private <T> void runOnDatabase(Supplier<T> task, Consumer<T> onSuccess, Runnable onError) {
    DATABASE_QUEUE.execute(() -> {
        try {
            T result = task.get();
            onActiveScreen(() -> onSuccess.accept(result));
        } catch (SQLException exception) {
            Log.e(TAG, "Ranking storage failed", exception);
            onActiveScreen(onError);
        }
    });
}

private void onActiveScreen(Runnable change) {
    mainHandler.post(() -> {
        if (!isDestroyed() && !isFinishing()) {
            change.run();
        }
    });
}
```

- `<T>` 表示这个方法可以处理不同结果类型。以读取排行榜为例，`T` 就是 `List<RankEntry>`。`Supplier<T>` 提供结果，所以可以传入 `() -> dao.list(requested)`。`Consumer<T>` 接收结果并执行后续操作。失败回调使用不需要参数的 `Runnable`。
- `DATABASE_QUEUE.execute(...)` 把操作排到后台，页面可以先展示「正在读取成绩」。完成后 `mainHandler.post(...)` 把刷新控件的动作交回主线程，页面已退出时就不再更新。
- `loadRanking()` 另外给每次查询分配一个递增的 `version`。如果用户刚点完「普通」又切到「困难」，普通榜的请求即使这时返回，版本号也对不上，页面会等最新请求的结果。

### 5. 一条记录如何显示成列表中的一行

`ListView` 滚动时会请求 `RankAdapter.getView()`。先看这个方法如何准备或复用一行。 [RankAdapter.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/rank/RankAdapter.java)

```java
View row = convertView;
if (row == null) {
    row = inflater.inflate(R.layout.row_rank, parent, false);
    row.setTag(new RowViews(
            row.findViewById(R.id.row_position), row.findViewById(R.id.row_name),
            row.findViewById(R.id.row_time), row.findViewById(R.id.row_score),
            row.findViewById(R.id.row_delete)));
}
```

- `convertView` 是 ListView 交来的旧行。如果它是 `null`，才用 `inflate()` 根据 `row_rank.xml` 创建一行。第三个参数 `false` 表示这里先创建，稍后由 ListView 把它放进列表。
- `RowViews` 记录这行的姓名、分数等控件引用，`setTag()` 把它附在行上。因此复用时可以用 `(RowViews) row.getTag()` 取回，省去再次查找控件。 [row_rank.xml](../../AircraftWar2026/app/src/main/res/layout/row_rank.xml)

接下来是同一个方法中填写成绩的部分。 [RankAdapter.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/rank/RankAdapter.java)

```java
RankEntry entry = getItem(position);
Context context = row.getContext();
boolean isFirst = position == 0;
row.setBackgroundResource(isFirst ? R.drawable.rank_leader : R.drawable.panel);
views.position().setText(scoreFormat.format(position + 1));
views.position().setTextColor(context.getColor(isFirst ? R.color.leader : R.color.accent));
views.name().setText(entry.name());
views.time().setText(dateFormat.format(new Date(entry.playedAt())));
views.score().setText(scoreFormat.format(entry.score()));
views.delete().setContentDescription(context.getString(R.string.delete_rank_accessible,
        entry.name()));
views.delete().setOnClickListener(view -> onDelete.accept(entry));
return row;
```

1. `position` 是当前结果中的位置，从 0 开始，因此第一名显示为 `position + 1`。它会随查询和排序改变，数据库行 ID 则保持稳定。
2. 无论这一行是新建还是复用，都重新填姓名、时间、分数和背景。否则一个原来显示第一名的行被滚动复用时，可能仍带着高亮。删除回调也重新绑定到这一行的 `entry`。
3. 数据查完后，页面把结果交给 `replaceRecords()`。它替换 Adapter 中的列表并调用 `notifyDataSetChanged()`，通知 ListView 重新读取行数和内容。

### 6. 删除后为何会重新排名

点某行的删除按钮，会先弹出确认框。只有确认才调用 `dao.delete(entry.id())`。DAO 使用如下条件定位行。 [SQLiteRankDao.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/rank/SQLiteRankDao.java)

```java
public void delete(long id) {
    getWritableDatabase().delete(TABLE, "_id = ?", new String[]{Long.toString(id)});
}
```

比如普通榜有两条都叫 Pilot 的 70 分记录。如果被点中的行 ID 是 8，SQLite 只删除 `_id = 8` 这一行。旧的榜单位置并未参与删除。页面在同一次后台任务中重新查询当前难度，主线程收到新列表时重新从第 1 名开始显示。删完最后一行时，ListView 显示空榜提示。 [RankActivity.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/RankActivity.java)

## 运行与验证

1. 从主菜单打开本地排行榜，确认空榜提示。用「添加记录」在简单榜录入不同分数，再录入两条同名同分记录。核对排序与高亮，删除其中一条，确认另一条还在。
2. 切换到困难榜，核对它与简单榜独立。强制停止应用再重开，确认已保存的记录仍能查到，删掉的记录没有回来。
3. 完成一局，然后点「记录成绩」。弹窗应显示本局分数，只需要输入姓名。试试输入全空格，观察验证提示，再输入合法姓名保存。
4. 连接模拟器或设备后运行 `./gradlew :app:connectedDebugAndroidTest`。设备测试覆盖难度过滤、同分排序、重开数据库、重复存分和精确删除。AndroidX Test Runner 与 JUnit 扩展属于 `androidTestImplementation`，只用于测试。可在 Android Studio 的 Device Explorer 查看应用创建的 `databases/rank.db`。

## 小结

拿一条成绩从头追踪，能看到三个职责交接点。弹窗把合法输入交给页面，DAO 读写 SQLite，Adapter 把查出的记录绑定到可复用的行。排序位置、数据库行 ID 和局编号在这条流程中各有用途。
