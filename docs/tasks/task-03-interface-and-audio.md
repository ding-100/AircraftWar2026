# 实验三 · 难度界面、游戏音频与结算

## 任务概述

应用启动时先展示难度选择。点击简单、普通或困难会进入对应关卡。主菜单提供音乐与音效开关。对局结束时展示本局得分，可以重玩或返回主菜单。

## 关键概念

- `Intent` 是 Android 请求打开页面时携带的信息。这里用显式 `Intent` 指定 `GameActivity`，并通过 extra（附加的键值参数）一起传入难度和音频开关。
- `Handler` 把消息放进线程的待办队列。`Looper.getMainLooper()` 取得在主线程上循环处理消息的对象，绑定它的 `Handler` 会让消息在主线程处理。绘制线程可以继续更新游戏，弹窗等界面操作由主线程完成。
- `MediaPlayer` 适合持续播放背景音乐，并可以暂停和接着播放。`SoundPool` 预加载短音效，让碰撞等反馈能快速播放，也能处理同一时刻的多个声响。
- `SharedPreferences` 保存少量设置。本项目以一个布尔值记录开关，下次启动会读回上一次设置。

## 实现流程

`MainActivity` 从 XML 加载控件并注册点击监听。写法 `view -> launch(Difficulty.NORMAL)` 是 Java 的 lambda，表示按钮被点击时执行 `launch()`。它随后在 `Intent` 中发送难度名称。游戏页读取难度，用同一个 `GameSession` 搭配不同的参数和背景。简单关卡敌机较少且无首领，普通和困难每增加 300 分就达到一个新的首领门槛。场上没有首领时才会生成下一只。困难模式还有周期性护盾。

`GameSession.update()` 收集本帧发生的事件。`BaseGame` 打包事件和分数发给 `GameActivity`。主线程收到消息后，按事件播放音效，收到 `GAME_OVER` 时显示本局结算。每局只发出一次结束事件。

## 关键代码

- `GameActivity.handleGameMessage()` 接收 `BaseGame.FrameEvents`。它是 Java 的 record，用来把事件和得分放在同一个对象里，Java 会生成 `events()` 和 `score()` 这样的访问方法。收到 `GAME_OVER` 后，页面直接读取消息里的得分。页面退出时清理待处理消息，页面销毁后到达的消息也会被忽略。[`GameActivity.java`](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/GameActivity.java)
- `GameAudio` 管理前台状态和音频焦点。当首领出现时，`MyMediaPlayer` 暂停普通曲目，播放首领曲目。首领离场后继续普通曲目。音频焦点由系统分配，离开游戏或失去焦点会暂停播放。[`GameAudio.java`](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/music/GameAudio.java) [`MyMediaPlayer.java`](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/music/MyMediaPlayer.java)
- `MySoundPool` 装载子弹命中、补给、炸弹和结束音效，等加载回调成功后才使用对应 ID。切到后台时暂停，销毁页面时释放两个音频组件。 [`MySoundPool.java`](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/music/MySoundPool.java)
- 一局结束后，重新开局会新建 `BaseGame` 和本局状态，并让音轨回到开头。屏幕旋转由同一个游戏页处理，绘制区域根据新尺寸换算。

## 运行与验证

1. 依次选择简单、普通和困难，核对页面标题、背景和出敌差异。
2. 开启声音，确认普通背景乐能循环，命中、补给和结束有音效。普通或困难达到 300 分时检查首领音乐，击败首领后应回到原来曲目。
3. 关闭声音再进入游戏。离开游戏后重新启动应用，检查开关状态仍然保留。
4. 与敌机相撞，核对结算分数和重玩后的初始生命值。按 Home 再返回，确认绘制和声音可以恢复。
5. 执行 `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`。单元测试覆盖命中事件、首领出现和每局仅发一次结束事件。

已在 API 36 模拟器验证难度切换、普通背景乐播放状态、静音设置保留、后台暂停和结算重玩。首领出现有单元测试覆盖，首领配乐和各短音效的实际听感仍待试听。

## 小结

难度与音频设置通过 `Intent` 传入对局，游戏事件通过 `Handler` 回到页面。规则只产生事件，音频组件按事件播放，页面负责显示结果。
