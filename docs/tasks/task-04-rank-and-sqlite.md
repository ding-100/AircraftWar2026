# 实验四 · 本地排行榜与 SQLite

## 任务概述

从主菜单进入本地排行榜，可以查看简单、普通和困难三份榜单。对局结束后选择「记录成绩」，输入姓名就能保存这局的实际分数。榜单也提供「添加记录」以独立验证录入功能。记录按分数降序排列，第一名高亮，每行支持确认后删除。

## 关键概念

- `ListView` 负责滚动和复用屏幕上的行。`BaseAdapter` 负责把一条成绩交给相应的行控件。`getView()` 拿到可复用的 `convertView` 时，会重新填写姓名、分数、排名和删除事件。
- DAO 是数据访问接口。桌面版 `DaoImpl` 会在运行目录读写各难度的文本文件，Android 版延续这种分工，通过 `RankDao` 调用 SQLite 实现。
- SQLite 是 Android 自带的本地数据库。应用通过 `SQLiteOpenHelper` 创建和打开数据库。成绩保存在应用私有的 `databases/rank.db`，应用重启后仍可读取。
- `Cursor` 是查询结果的游标。每次调用 `moveToNext()` 读一行，离开 `try` 代码块时 Java 自动关闭游标。

## 实现流程

1. `GameActivity` 从游戏线程的结束消息拿到本局得分，再通过 `Intent` 把难度、得分和局编号传给 `RankActivity`。
2. `RankEntryDialog` 检查姓名和分数。结算入口使用本局得分，手动录入入口要求输入非负整数。页面把查询和写入交给同一个数据库工作队列。
3. `SQLiteRankDao` 按难度过滤，通过 SQL 排序。同分时早记录在前，时间也相同则按记录 ID 排序。读完后回到主线程，`RankAdapter.replaceRecords()` 调用 `notifyDataSetChanged()` 更新列表。
4. 确认删除后，DAO 根据记录 ID 移除一条记录，再查询并刷新当前榜单。删完最后一条会显示空状态。

## 关键代码

- `SQLiteRankDao.onCreate()` 定义成绩表和排序索引。难度、局编号、玩家姓名、分数和时间都有独立的列，SQLite 可以直接筛选和排序。第一名与行号由最新的查询顺序决定。
- 局编号在开局时生成，数据库用 `UNIQUE` 约束检查它。若页面恢复后又提交同一局，SQLite 保留已有记录。列表中的删除按钮绑定数据库行 ID，同名同分的两条成绩可以分别处理。
- `RankAdapter` 用行视图的 `tag` 保存控件引用，复用时省去重复查找控件。它会同时重设颜色和背景，第一名的高亮不会跟着复用行滚到别的位置。
- `RankActivity` 使用后台队列处理 SQLite，再通过主线程 `Handler` 更新控件。快速切换难度时，旧查询即使先完成，也不会覆盖当前榜单。

## 运行与验证

1. 从主菜单打开「本地排行榜」，确认空榜提示。用「添加记录」在简单榜录入两条不同分数，检查顺序与第一名高亮，再切换困难确认榜单独立。
2. 删除任意一条记录，确认列表更新。强制停止后重启，确认保存仍在、被删除的记录仍已移除。
3. 完成一局游戏后点击「记录成绩」，输入名字后保存。对应难度会显示本局分数。取消录入可以继续浏览榜单。
4. 使用 `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` 验证构建。连接模拟器或设备后执行 `./gradlew :app:connectedDebugAndroidTest`，运行 SQLite 的难度过滤、同分排序、重开持久化、重试和精确删除测试。
5. 在 Android Studio 的 Device Explorer 中查看 `edu.hitsz.aircraftwar/databases/rank.db`。数据库实际创建后才能看到该文件。

## 小结

列表展示、行绑定和 SQLite 操作由独立职责承担。数据库负责筛选、排序和唯一性约束，适配器依据查出的记录更新视图。
