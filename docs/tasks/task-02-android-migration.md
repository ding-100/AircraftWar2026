# 实验二 · 从桌面迁移到 Android

## 任务概述

把桌面版飞机大战的素材和游戏规则迁移到 Java 编写的 Android 项目 `AircraftWar2026`。实验二结束时，应用能在 Android 11（API 30）及以上设备运行，玩家可以拖动英雄机，画面会滚动，命中敌机会计分。

课程要求按四步迁移。表格记录的是各步提交当时可看到的效果。打开当前 `main` 会先看到实验三加入的主菜单，选「简单」即可进入同一套游戏循环。

| 阶段 | 当时的效果 | 提交 |
| --- | --- | --- |
| 创建工程 | 启动后显示应用名 | `632c6ed` |
| 迁移资源 | 显示背景和英雄机的静态预览 | `0086769` |
| 接入绘制循环 | 背景开始滚动 | `5cc5861` |
| 接入输入和规则 | 能拖动、射击、碰撞和计分 | `56d557d` |

## 关键概念

- **Activity 和生命周期**。Activity 是 Android 管理的一个界面。系统创建界面时调用 `onCreate()`，进入可交互状态时调用 `onResume()`，失去该状态时调用 `onPause()`。这些由系统在合适时机调用的方法叫回调。
- **资源 ID**。布局、图片、文字放在 `res/` 下。构建时生成 `R` 类，Java 通过 `R.drawable.hero` 等整数编号找到资源。图片置于 `drawable-nodpi`，Android 会保留素材像素尺寸，由游戏统一控制缩放。
- **SurfaceView 和 Canvas**。SurfaceView 提供独立的绘制区域，Canvas 是绘制一帧时拿到的画布。游戏子线程可以在这块画布上持续绘制，主线程负责处理触摸和页面操作。
- **逻辑坐标**。游戏规则使用固定的 512 × 768 场地。敌机速度、子弹位置和碰撞区域都基于这套坐标。绘制时再换算到设备的实际像素。

## 实现概览

手指拖动时，Android 把触摸事件交给 `BaseGame`。它把触摸点换算成逻辑坐标并保存。游戏子线程在下一步读取坐标，调用 `GameSession` 移动英雄机、生成敌机和处理碰撞，再交给 `GameRenderer` 画出当前状态。

先读下面对应的片段，再打开链接查看完整文件。代码块节选自当前源码，用到的字段和其他方法可在同一个链接中找到。

## 关键代码

### 1. 页面如何显示布局

`MainActivity` 的 `onCreate()` 中有下面三行。当前布局会在实验三接入难度按钮，页面创建方式从实验二第一步沿用至今。 [MainActivity.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/MainActivity.java)

```java
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_main);
ScreenInsets.applyTo(findViewById(R.id.menu_root));
```

1. `MainActivity extends Activity` 表示它继承了 Activity 的行为。它可以重写 `onCreate()`，也可以通过 `super.onCreate(...)` 先运行父类的初始化。方法上的 `@Override` 会让编译器检查它是否真的在重写一个父类方法。
2. `setContentView()` 接收布局资源 ID，Android 根据 XML 创建控件。这里的 `R.layout.activity_main` 对应 `res/layout/activity_main.xml`。
3. `findViewById()` 在刚创建的界面里找已存在的控件。`ScreenInsets` 给页面留出状态栏和导航栏需要的空间。因此这行需要放在加载布局之后。

### 2. 图片如何从资源变成能绘制的对象

打开一局游戏时，`GameRenderer` 会创建 `GameImages`。下面是 `GameImages` 的构造方法，负责准备本局使用的图像。 [GameImages.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/GameImages.java)

```java
GameImages(Resources resources, Difficulty difficulty) {
    int backgroundId = switch (difficulty) {
        case EASY -> R.drawable.background_easy;
        case NORMAL -> R.drawable.background_normal;
        case HARD -> R.drawable.background_hard;
    };
    background = BitmapFactory.decodeResource(resources, backgroundId);
    for (Sprite sprite : Sprite.values()) {
        sprites.put(sprite, BitmapFactory.decodeResource(resources, drawableId(sprite)));
    }
}
```

- 构造方法与类同名，调用 `new GameImages(...)` 时执行。`resources` 让代码访问应用资源，`difficulty` 用来选择背景。
- 这里的 `switch` 是表达式。比如难度为 `NORMAL`，这一组分支最后产生 `R.drawable.background_normal` 这个值，赋给 `backgroundId`。
- `decodeResource()` 把图片资源读成 内存中的 `Bitmap`。增强 `for` 循环会把 `Sprite.values()` 中的图像类型逐个取出来，比如英雄机和子弹。`sprites` 是一张按类型查图的表，键是 `Sprite`，值是 `Bitmap`。一次读取后，每帧直接取已经准备好的图片。

### 3. 游戏什么时候启动线程

`BaseGame extends SurfaceView implements SurfaceHolder.Callback, Runnable` 中，`extends` 表示继承绘图控件，`implements` 表示遵守接口的方法约定。Android 会在绘制区域就绪时调用 `surfaceCreated()`，而 Java 会在线程启动后调用 `Runnable` 约定的 `run()`。两个入口会在不同的时机发生，所以启动线程前要先检查状态。 [BaseGame.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/BaseGame.java)

```java
private void startLoopIfReady() {
    if (!isScreenActive || !isSurfaceReady || gameThread != null) {
        return;
    }
    isRunning = true;
    gameThread = new Thread(this, "AircraftWar-Game");
    gameThread.start();
}
```

- `!` 表示取反，`||` 表示「任一条件满足」。页面还不可交互、画布还没准备好、已经有线程这三个条件中，只要有一个成立，`return` 就提前结束方法。
- `this` 是当前 `BaseGame` 对象。由于它实现了 `Runnable`，可以把它传给 `Thread`。调用 `start()` 后，新线程才会执行它的 `run()`。
- 进入后台时，游戏线程完成当前帧后通过 `wait()` 等待，恢复时由 `notifyAll()` 唤醒。如果系统要销毁绘制区域，`surfaceDestroyed()` 则会请求线程退出并等待它结束，确保画布已经归还。

### 4. 一帧是怎样画到屏幕上的

下面是 `BaseGame.drawFrame()`。画布的占用时间只覆盖这一帧的绘制。 [BaseGame.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/BaseGame.java)

```java
private void drawFrame() {
    SurfaceHolder holder = getHolder();
    Canvas canvas = holder.lockCanvas();
    if (canvas == null) {
        return;
    }
    try {
        Viewport current = viewport;
        canvas.drawColor(borderColor);
        canvas.translate(current.left(), current.top());
        canvas.scale(current.scale(), current.scale());
        canvas.clipRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        renderer.draw(canvas, session);
    } finally {
        holder.unlockCanvasAndPost(canvas);
    }
}
```

1. `getHolder()` 取得管理绘制区域的对象。`lockCanvas()` 临时借到画布。如果当前拿不到画布，`canvas == null` 就跳过这帧。
2. `translate()` 把画布原点移到场地左上角，`scale()` 把逻辑尺寸转换成屏幕尺寸，`clipRect()` 只保留游戏区域内的绘制。随后 `renderer.draw()` 读取本局状态，依次绘制背景、飞机、子弹和 HUD（屏幕上的生命、分数等信息）。滚动背景用同一张图上下接起来。
3. `finally` 是 Java 的收尾代码块。即使画图途中抛出异常，也会尝试归还画布。调用 `unlockCanvasAndPost()` 后，本帧才会交给系统显示。 [GameRenderer.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/GameRenderer.java)

### 5. 手指位置为什么要换算

`MotionEvent` 保存 Android 发来的触摸信息，例如按下、移动或抬起。`onTouchEvent()` 取其中的像素坐标，再逆向应用绘制用的平移和缩放。 [BaseGame.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/BaseGame.java)

```java
Viewport current = viewport;
if (current.scale() > 0) {
    target = new TouchPosition(
            Math.round((event.getX() - current.left()) / current.scale()),
            Math.round((event.getY() - current.top()) / current.scale()));
}
```

- 假设绘制区域为 1080 × 1920 像素。512 × 768 的场地等比放大后为 1080 × 1620，上下各留约 150 像素。触摸点 `(540, 960)` 减去偏移，再除以约 2.109 的缩放比例，就等于逻辑坐标 `(256, 384)`。`Math.round()` 把结果四舍五入成整数。
- `TouchPosition` 把横纵坐标放在同一个不可变对象里。`target` 上的 `volatile` 让主线程写入的新对象对之后读取它的游戏线程可见。主线程只发布目标，实际飞机位置由游戏线程修改。
- 英雄机坐标记录的是中心点。`HeroAircraft.moveTo()` 会用半个机身的宽、高限制坐标，避免手指拖到边缘时机身有一半出界。 [HeroAircraft.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/game/aircraft/HeroAircraft.java)

### 6. 命中、扣血、计分有什么顺序

`GameSession.update()` 负责推进一小步，目标间隔约 40 毫秒。每步依次生成与移动对象、判断碰撞并清理失效对象。以下是英雄机子弹命中敌机的完整方法。 [GameSession.java](../../AircraftWar2026/app/src/main/java/edu/hitsz/aircraftwar/game/GameSession.java)

```java
private void resolveHeroBullets(EnumSet<GameEvent> events) {
    for (BaseBullet bullet : heroBullets) {
        if (bullet.notValid()) {
            continue;
        }
        for (AbstractEnemy enemy : enemies) {
            if (!enemy.notValid() && enemy.crash(bullet)) {
                bullet.vanish();
                enemy.decreaseHp(bullet.getPower());
                events.add(GameEvent.BULLET_HIT);
                if (enemy.notValid()) {
                    score += enemy.getPoints();
                    dropSupplies(enemy);
                }
                break;
            }
        }
    }
}
```

1. 外层循环逐颗检查英雄机子弹。`continue` 表示跳到下一颗。如果子弹还有效，内层循环才检查它与每架敌机是否碰撞。`&&` 两侧条件都成立，才算一次有效命中。
2. 命中时先标记这颗子弹失效，再给敌机扣血。普通敌机初始生命是 30，英雄机普通子弹的伤害是 50。扣到 0 后敌机会失效，此时 `score += enemy.getPoints()` 会加上普通敌机的 10 分。其他敌机的击落分是 20 分，道具掉落由敌机类型决定。
3. `break` 只退出内层循环，所以同一颗子弹到此为止，外层还会检查其他子弹。循环内先做标记，等碰撞检查结束后再移除失效对象，避免在遍历同一列表时改变它的内容。机身碰撞另有检查，所以屏幕上没有英雄机子弹时，相撞仍会结算。`events` 记录「命中」等事件，实验三会用它播放音效。

## 运行与验证

1. 在纯英文路径下用 Android Studio 打开 `AircraftWar2026`，安装 API 36 SDK，选择 API 30 或以上的模拟器或真机，运行 `app`。Java 语言级别为 17，`minSdk = 30` 决定最低安装版本。 [app/build.gradle.kts](../../AircraftWar2026/app/build.gradle.kts)
2. 进入简单关卡。左右拖到场地边缘，观察机身是否完整留在画面内。对准一架普通敌机，观察命中后消失、分数加 10。按 Home 再回到同一局，观察动画和进度是否恢复。
3. 在项目目录运行 `./gradlew :app:assembleDebug :app:testDebugUnitTest`。JUnit 依赖用于电脑上的规则测试。用 `git log --oneline --grep='task-02'` 可以回看各阶段历史。

## 实现中遇到的问题

机身碰撞单测最初让英雄机一直待在敌机生成点附近，自动发射的子弹先击落了敌机。用例改为先在初始位置等敌机生成，再把英雄机移到同一条航线上，才真正验证到机身相撞。

## AI 使用日志

| 环节 | 原本的思路 | AI 协助和实际采纳 |
| --- | --- | --- |
| 工程空壳 | 从 Empty Views Activity 起步 | 对照课程示例确认清单、启动 Activity 和布局的关系，保留 Java 与最低 API 30，采用系统自带的 View 控件。 |
| 图片加载 | 沿用桌面版的原始图片 | 将按文件路径读取改为按 Android 资源 ID 解码，用独立预览验证装包。 |
| 游戏线程 | 沿用桌面版定时刷新思路 | 通过 Surface 生命周期控制子线程，加入空画布判断和 `finally` 解锁，等待旧线程结束后再允许创建新线程。 |
| 交互与逻辑 | 继续使用桌面版坐标、射击策略和计分 | 统一换算逻辑坐标，用游戏步数计时，并补测边界、命中和机身碰撞。 |

## 小结

沿着「触摸输入 → 逻辑坐标 → 更新状态 → 绘制一帧」回看实验，就能分清哪些是桌面游戏可复用的规则，哪些要借助 Android 的画布和触摸 API 实现。
