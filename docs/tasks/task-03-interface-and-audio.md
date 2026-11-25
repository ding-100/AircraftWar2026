# 实验三 · 难度界面、游戏音频与结算

## 任务概述

在实验二的游戏循环上加入主菜单。玩家能选择简单、普通或困难，改变音乐开关，并在对局结束时查看分数、重玩或返回。当前版本还通过实验四接入了「记录成绩」。

## 关键概念

- **XML 布局和 View**。XML 描述按钮等控件长什么样、放在哪里。Android 创建这些控件后，Java 可以找到它们并设置点击行为。一个能显示或接收操作的控件通常是一个 View。
- **Intent**。请求 Android 执行某个操作的信息对象。这里直接指定了要打开的 Activity，再附带难度和音频开关。这些附带的键值参数叫 extra。
- **Handler 和 Looper**。Handler 可以把消息放入某个线程的待办队列，Looper 在所属线程上循环处理这些消息。游戏线程把事件发给主线程的 Handler，主线程才能安全地显示对话框。
- **MediaPlayer 和 SoundPool**。MediaPlayer 适合持续播放背景音乐，可以暂停并从原位置继续。SoundPool 适合预加载短音效，并在命中、捡道具等事件发生时播放。
- **SharedPreferences**。Android 提供的少量键值设置存储。本项目用它记住音乐开关，下次打开应用还能读回来。

## 实现概览

按「普通」后，`MainActivity` 用 Intent 打开 `GameActivity`。游戏页读取难度，用它创建 `BaseGame` 和本局状态。玩游戏时，`GameSession` 收集每帧事件，`BaseGame` 用 Handler 发给游戏页。游戏页根据事件更新音乐和短音效，收到结束事件后显示结算。

下面的代码块节选自当前源码，可按链接找到完整文件。

## 关键代码

### 1. XML 里的按钮如何连到 Java

这是「普通」按钮的布局。 [activity_main.xml](../../AircraftWar2026/app/src/main/res/layout/activity_main.xml)

```xml
<Button
    android:id="@+id/play_normal"
    style="@style/Widget.AircraftWar.PrimaryButton"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_marginStart="@dimen/space_8"
    android:layout_weight="1"
    android:text="@string/difficulty_normal" />
```

- `@+id/play_normal` 为按钮声明了名字，构建后 Java 可以用 `R.id.play_normal` 查到它。`@string/difficulty_normal` 则引用另一种资源，显示的是「普通」。
- 三个难度按钮并排放在横向容器里。`dp` 是 Android 布局的尺寸单位，会随设备屏幕密度换算成像素。这里的 `0dp` 配合 `layout_weight="1"`，表示宽度交给容器按权重分配。三个按钮权重相同，因此平分留给按钮的宽度。
- 按钮本身只知道外观。下面三行把点击操作注册到对应按钮上。 [MainActivity.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/MainActivity.java)

```java
findViewById(R.id.play_easy).setOnClickListener(view -> launch(Difficulty.EASY));
findViewById(R.id.play_normal).setOnClickListener(view -> launch(Difficulty.NORMAL));
findViewById(R.id.play_hard).setOnClickListener(view -> launch(Difficulty.HARD));
```

`view -> launch(Difficulty.NORMAL)` 是一个 lambda。箭头左边的 `view` 是被点击的控件，由 Android 传入。箭头右边是点击发生时要执行的动作。这行代码先把动作交给控件，等玩家点击才调用 `launch()`。`Difficulty` 是 Java 的枚举，即一组有限、可命名的值，这里有 `EASY`、`NORMAL` 和 `HARD`。 [Difficulty.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/game/Difficulty.java)

### 2. 下一页怎样知道玩家选了「普通」

`MainActivity.launch()` 调用下面的方法构造 Intent，再把返回值交给 `startActivity()`。 [GameActivity.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/GameActivity.java)

```java
static Intent createIntent(Context context, Difficulty difficulty, boolean isSoundEnabled) {
    return new Intent(context, GameActivity.class)
            .putExtra(EXTRA_DIFFICULTY, difficulty.name())
            .putExtra(EXTRA_SOUND_ENABLED, isSoundEnabled);
}
```

1. `static` 表示可以直接用类名调用这个方法。此时游戏页还没有创建，`GameActivity.createIntent(...)` 就可以先描述要打开哪个页面。
2. `Context` 提供当前 Android 运行环境，这里由主菜单传入自己的 Activity。`GameActivity.class` 指定要启动的类，具体的 Activity 对象由 Android 创建。
3. `putExtra()` 把参数按键名放进 Intent，并返回同一个 Intent，所以可以连续调用。若选「普通」，`difficulty.name()` 放进去的是字符串 `NORMAL`，音频开关则保存为 `boolean`（只能是 `true` 或 `false`）。
4. 游戏页的 `onCreate()` 使用 `Difficulty.fromName(getIntent().getStringExtra(EXTRA_DIFFICULTY))` 把字符串转回枚举，使用 `getBooleanExtra(EXTRA_SOUND_ENABLED, true)` 读开关。接下来选定背景和出敌参数。普通和困难的首领分数门槛每隔 300 分一个，场上已有首领时会等它离场。

### 3. 音乐开关为什么重启后还在

在主菜单，切换开关会调用 `MusicSettings.setEnabled()`，打开菜单时用 `isEnabled()` 初始化开关。 [MusicSettings.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/MusicSettings.java)

```java
static boolean isEnabled(Context context) {
    return preferences(context).getBoolean(AUDIO_ENABLED, true);
}

static void setEnabled(Context context, boolean isEnabled) {
    preferences(context).edit().putBoolean(AUDIO_ENABLED, isEnabled).apply();
}
```

- 两个方法都使用同一个键 `AUDIO_ENABLED`。首次使用时还没有保存这个键，所以 `getBoolean(..., true)` 返回默认值 `true`。
- `edit()` 取得设置编辑器，`putBoolean()` 放入新值，`apply()` 立即更新内存并安排写入磁盘。当用户下一次开局，页面把最新设置随 Intent 传给游戏页。关闭时游戏页不会创建音频对象。

### 4. 绘制线程怎样通知页面结算

`GameSession.update()` 返回 `EnumSet<GameEvent>`。尖括号表示集合里装的是哪类元素。这里的 `EnumSet` 专门存放枚举，某一帧即使命中多次，集合也只会有一个 `BULLET_HIT`。下面的代码在绘图线程里运行。 [BaseGame.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/BaseGame.java)

```java
EnumSet<GameEvent> events = session.update();
drawFrame();
if (!events.isEmpty()) {
    FrameEvents frame = new FrameEvents(events, session.getScore());
    screenHandler.obtainMessage(FRAME_EVENTS, frame).sendToTarget();
}
```

- `FrameEvents` 是一个 Java record，把本帧事件和当时的分数组成一个对象。Java 根据 record 的声明生成构造方法以及 `events()`、`score()` 这样的取值方法。
- `obtainMessage(FRAME_EVENTS, frame)` 创建要发送的消息，`FRAME_EVENTS` 是消息类型，`frame` 是携带的数据。`sendToTarget()` 将这条消息交给 Handler 的队列，绘制线程随后可以继续执行。

游戏页用 `new Handler(Looper.getMainLooper(), this::handleGameMessage)` 建立主线程 Handler。`this::handleGameMessage` 是方法引用，表示消息轮到执行时，调用这个 Activity 的 `handleGameMessage()`。 [GameActivity.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/GameActivity.java)

```java
private boolean handleGameMessage(Message message) {
    if (message.what != BaseGame.FRAME_EVENTS || isFinishing() || isDestroyed()) {
        return false;
    }
    BaseGame.FrameEvents frame = (BaseGame.FrameEvents) message.obj;
    if (audio != null) {
        audio.playEvents(frame.events());
    }
    if (frame.events().contains(GameEvent.GAME_OVER)) {
        finalScore = frame.score();
        showResult();
    }
    return true;
}
```

1. `message.what` 是发送时填写的类型。先确认消息属于游戏帧，界面也没有关闭，再往下处理。
2. `message.obj` 可携带各种对象。括号中的 `BaseGame.FrameEvents` 是强制类型转换，告诉 Java 这里要按实际放进去的类型取值。检查类型编号后再转换，有助于让发送端与接收端对齐。
3. 如果有音频对象，先按事件播放声音。包含 `GAME_OVER` 时，用消息中的分数设置 `finalScore`，再弹出结算。返回 `true` 表示该回调已处理这条消息。每局只产生一次结束事件。

### 5. 首领配乐如何接替普通配乐

`GameAudio.playEvents()` 根据 `BOSS_ENTERED`、`BOSS_LEFT` 和 `GAME_OVER` 更新状态。只有页面在前台且获得音频焦点时才允许播放。音频焦点由系统协调，例如其他应用需要播放声音时，游戏可能暂时失去焦点。 [GameAudio.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/music/GameAudio.java)

```java
private void sync() {
    if (isBossActive) {
        regular.setPlaying(false);
        boss.setPlaying(canPlay);
    } else {
        boss.setPlaying(false);
        regular.setPlaying(canPlay);
    }
}
```

这是 `MyMediaPlayer` 的曲目切换逻辑。普通曲和首领曲各有一个播放器。首领出现时先暂停普通曲，再按当前播放许可决定是否启动首领曲。首领离场时反过来操作，普通曲从暂停的位置继续。对局结束时背景乐会暂停。音频未准备好时 `setPlaying()` 会先返回，准备完成的回调再尝试同步播放状态。 [MyMediaPlayer.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/music/MyMediaPlayer.java)

### 6. 短音效为什么要等加载回调

`MySoundPool` 用一张表保存事件与音效编号的对应关系，例如 `BULLET_HIT` 对应子弹命中音效。文件加载成功后，回调会把编号加入 `loaded` 集合，播放前才能检查它是否准备好。 [MySoundPool.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/music/MySoundPool.java)

```java
void play(GameEvent event) {
    Integer id = sounds.get(event);
    if (loaded.contains(id)) {
        int priority = event == GameEvent.GAME_OVER ? 2 : 1;
        soundPool.play(id, 0.8f, 0.8f, priority, 0, 1f);
    }
}
```

- `Integer` 是整数的对象类型。和 `int` 相比，它可以用 `null` 表示「表里找不到这个事件」。找不到或还没加载时，条件不成立，就跳过这次播放。
- `? :` 是条件表达式。游戏结束音效的优先级为 2，其他音效为 1。`play()` 的两个 `0.8f` 是左右声道音量，后面的 `0` 表示不循环，`1f` 表示正常速度。
- 切到后台会暂停音频，销毁游戏页会释放播放器。再玩一局时，游戏页创建新的 `BaseGame`，状态重新开始，曲目也回到开头。

## 运行与验证

1. 从主菜单依次选择三档难度，核对标题、背景和出敌差异。
2. 开启声音，检查普通背景乐和命中、补给、结束音效。普通或困难达到首领门槛后试听曲目切换，首领离场后应回到普通曲。
3. 关闭音乐与音效，重新启动应用，确认开关记住了上次状态。打开声音进入游戏，再按 Home 离开并返回，检查游戏和声音能否恢复。
4. 与敌机相撞，核对最终分数，点击「再玩一局」检查初始生命值。运行 `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` 可以检查构建和游戏事件测试。

已在 API 36 模拟器验证难度切换、普通背景乐播放状态、静音设置保留、后台暂停和结算重玩。首领出现有单元测试覆盖，首领配乐和各短音效的实际听感仍待试听。

## 小结

按钮监听把用户选择交给 Intent，Intent 把参数送到游戏页。游戏线程则用 Handler 送回本帧事件。理解这两次传递后，就能分别定位难度参数、音效触发和结算页面的代码。
