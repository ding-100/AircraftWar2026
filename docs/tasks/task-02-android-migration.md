# 实验二 · 从桌面迁移到 Android

## 任务概述

使用 Java 创建 `AircraftWar2026`，支持 Android 11（API 30）及以上。以课程目录中的桌面版 `AircraftWar` 为迁移来源，按空壳、资源、游戏循环、交互与规则四步提交。

## 关键概念

- `Activity` 是 Android 管理的一个界面。它的生命周期是一组由系统调用的方法，例如进入前台时调用 `onResume()`，离开可交互状态时调用 `onPause()`。
- `SurfaceView` 是能在子线程绘图的界面控件。绘制代码先取得 `Canvas` 画布，在上面画图片和文字，再提交整帧。这里把需要频繁执行的游戏计算和绘图放在同一个子线程，给主线程留出处理触摸和页面操作的时间。
- `SurfaceHolder.Callback` 和 `Runnable` 都是 Java 接口。接口约定实现类要提供哪些方法。Android 在画布变化时调用第一个接口的方法，Java 线程启动时调用第二个接口的 `run()`。这种由系统或框架在某个时机主动调用的方法叫回调。

## 实现概览

实验二最后一步的 `MainActivity` 直接进入简单难度。系统创建绘制区域后，`BaseGame` 启动子线程。主线程把最新的触摸位置交给子线程，子线程每步让 `GameSession` 更新飞机、子弹和分数，再由 `GameRenderer` 绘出本帧。后续实验将主菜单和结算接入这条流程。下面四步对应实验二的历史提交，运行当前 `main` 会先进入主菜单。

## 第一步 · 创建工程

在 Android Studio 中打开 `AircraftWar2026`，同步 Gradle 后运行 `app`。此步启动后会显示应用名。Gradle Wrapper 固定构建工具的版本。项目使用 Java 17 语言级别和 API 36 SDK 编译，安装门槛由 `minSdk = 30` 决定。Gradle 中的 JUnit 依赖只用于在电脑上跑规则测试。系统读取 `AndroidManifest.xml` 后找到启动 Activity，调用 `MainActivity.onCreate()`，再由 `setContentView()` 加载 XML 布局。

## 第二步 · 迁移资源

将桌面版的关卡背景、飞机、子弹、道具和护盾复制到 `res/drawable-nodpi`，驼峰命名改为小写加下划线。`nodpi` 让 Android 保留素材原始像素尺寸，渲染时统一缩放。Gradle 为资源生成 `R.drawable` 常量，`GameImages` 通过 `BitmapFactory.decodeResource()` 把资源解码成可绘制的 `Bitmap`。此步启动后能看到背景与英雄机的静态预览。

## 第三步 · 启动绘制循环

`SurfaceView` 提供可独立绘制的屏幕区域。在 `BaseGame` 中实现 `SurfaceHolder.Callback` 接收区域创建、缩放、销毁的通知，并实现 `Runnable` 作为游戏线程的入口。页面可交互且绘制区域就绪时启动线程。每帧 `lockCanvas()` 取得画布，绘制后在 `finally` 中调用 `unlockCanvasAndPost()` 交还画布并展示。

`surfaceChanged()` 用屏幕宽高计算缩放比例，将桌面版的 512 × 768 坐标等比映射到手机屏幕。背景绘制两次，一张向下滚动，另一张接在上方，让首尾连续。切到后台时，绘制线程完成当前帧后调用 `wait()` 等待。页面恢复时调用 `notifyAll()` 唤醒线程，继续同一局。销毁绘制区域时，`surfaceDestroyed()` 才等待绘制线程退出，以保证系统回收画布前已经归还了它。

## AI 使用日志

| 环节 | 原本的思路 | AI 协助和实际采纳 |
| --- | --- | --- |
| 工程空壳 | 从 Empty Views Activity 起步 | 对照课程示例确认清单、启动 Activity 和布局的关系，保留 Java 与最低 API 30，采用系统自带的 View 控件。 |
| 图片加载 | 沿用桌面版的原始图片 | 将按文件路径读取的方式改为按 Android 资源 ID 解码，使用独立预览验证装包。 |
| 游戏线程 | 沿用桌面版定时刷新思路 | 通过 Surface 生命周期驱动子线程，加入空画布判断和 finally 解锁，并等待旧线程结束后再允许创建新线程。 |
| 交互与逻辑 | 继续使用桌面版坐标、射击策略和计分 | 将输入坐标统一换算到逻辑场地，移除每个道具的独立定时器，并补测边界、命中和机身碰撞。 |

## 第四步 · 接回交互与游戏规则

| 模块 | 桌面代码与 Android 端做法 |
| --- | --- |
| 移动、碰撞、射击 | `AbstractFlyingObject` 保留基于中心点与矩形交叠的判定，`ShootStrategy` 保留直射、散射、环射。碰撞和边界使用统一的游戏坐标。 |
| 敌机、计分、道具 | `RandomEnemySpawner` 按桌面参数创建四种敌机，`GameSession` 更新对象并处理碰撞。普通敌机击落加 10 分，其余加 20 分。道具持续时间用游戏步数计算，切到后台暂停计时。 |
| 输入与绘制 | `BaseGame.onTouchEvent()` 把屏幕坐标换算成游戏坐标，绘制线程读取位置并限制飞机在场内。`GameRenderer` 用 `Canvas` 绘制背景、飞机、弹幕、生命和分数。 |
| 页面、声音、排行榜 | 难度选择、音频与榜单分别由实验三和四接入。实验二运行时直接进入简单难度。 |

`GameSession.update()` 约每 40 毫秒执行一次。它先生成和移动对象，再判定碰撞、计分、移除失效对象。子弹命中即失效，避免同一颗子弹连续伤害重叠的敌机。机身碰撞单独检查，即使屏幕上没有英雄机子弹也会生效。每局创建自己的英雄机，重新开局会重置血量和分数。

触摸事件来自 Android 主线程，游戏状态由绘制线程统一修改。这里用 `volatile` 发布不可变的坐标对象，绘制线程每步读取一次，使同一帧的横纵坐标来自同一个触摸事件。

## 关键代码

- `BaseGame.onTouchEvent()` 先减去绘制区域在 View 内的左上角偏移，再除以绘制比例。以可用区域 1080 × 1920 为例，512 × 768 的场地等比放大后是 1080 × 1620，上下各留 150 像素。View 中心的触摸点 `(540, 960)` 因此对应逻辑场地的 `(256, 384)`。同一套游戏规则可以在不同尺寸的屏幕上运行。[`BaseGame.java`](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/BaseGame.java)
- `GameSession.update()` 更新敌机、子弹和道具，再判定碰撞。击落普通敌机时先让命中的子弹失效，再加 10 分。机身碰撞有自己的检查，因此没有英雄机子弹时，两架飞机相撞依然会结束对局。[`GameSession.java`](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/game/GameSession.java)

## 运行与验证

1. 在纯英文路径下用 Android Studio 打开 `AircraftWar2026`，安装 API 36 SDK，再选择 API 30 或以上的模拟器或真机，运行 `app`。命令行在同一目录执行 `./gradlew :app:assembleDebug :app:testDebugUnitTest`。
2. 查看背景滚动、敌机进入、子弹自动发射与计分。拖动英雄机到边缘，整架飞机应留在可玩区域。
3. 观察敌机出界、子弹命中、机身相撞。碰撞后的状态变化应在本局画面显示。切到后台再返回，当前画面和进度应继续。
4. Android Studio 的 Logcat 可按应用包名 `edu.hitsz.aircraftwar` 筛选。四步提交均可用 `git log --oneline --grep='task-02'` 找到并回看。

## 实现中遇到的问题

机身碰撞单测最初让英雄机一直待在敌机生成点附近，自动发射的子弹先击落了敌机，导致用例没有触发要测的机身碰撞。用例改为先在初始位置等敌机生成，再把英雄机移到同一条航线上。这能把机身相撞与已有弹幕命中区分开。

## 小结

迁移时可以保留游戏规则和 Java 类型，用 Android 资源、画布与输入事件接回新平台。逻辑坐标同时服务绘制和碰撞，平台线程只负责把输入交给游戏循环，规则可以在电脑上用单元测试重复验证。
