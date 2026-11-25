# 实验二 · 从桌面迁移到 Android

## 任务概述

使用 Java 创建 `AircraftWar2026`，支持 Android 11（API 30）及以上。以课程目录中的桌面版 `AircraftWar` 为迁移来源，按空壳、资源、游戏循环、交互与规则四步提交。

## 第一步 · 创建工程

在 Android Studio 中打开 `AircraftWar2026`，同步 Gradle 后运行 `app`。此步启动后会显示应用名。Gradle Wrapper 固定构建工具的版本，`app/build.gradle.kts` 指定 Android SDK 和 Java 版本。系统读取 `AndroidManifest.xml` 后找到启动 Activity，调用 `MainActivity.onCreate()`，再由 `setContentView()` 加载 XML 布局。

## 第二步 · 迁移资源

将桌面版的关卡背景、飞机、子弹、道具和护盾复制到 `res/drawable-nodpi`，驼峰命名改为小写加下划线。`nodpi` 让 Android 保留素材原始像素尺寸，渲染时统一缩放。Gradle 为资源生成 `R.drawable` 常量，`GameImages.decode()` 通过 `BitmapFactory.decodeResource()` 把资源解码成可绘制的 `Bitmap`。此步启动后能看到背景与英雄机的静态预览。

## 第三步 · 启动绘制循环

`SurfaceView` 提供可独立绘制的屏幕区域。在 `BaseGame` 中实现 `SurfaceHolder.Callback` 接收区域创建、缩放、销毁的通知，并实现 `Runnable` 作为游戏线程的入口。页面可交互且绘制区域就绪时启动线程。每帧 `lockCanvas()` 取得画布，绘制后在 `finally` 中调用 `unlockCanvasAndPost()` 交还画布并展示。

`surfaceChanged()` 用屏幕宽高计算缩放比例，将桌面版的 512 × 768 坐标等比映射到手机屏幕。背景绘制两次，一张向下滚动，另一张接在上方，让首尾连续。退出页面或切到后台会暂停循环，销毁绘制区域前等待绘制线程结束。

## AI 使用日志

| 环节 | 原本的思路 | AI 协助和实际采纳 |
| --- | --- | --- |
| 工程空壳 | 从 Empty Views Activity 起步 | 对照课程示例确认清单、启动 Activity 和布局的关系，保留 Java 与最低 API 30，采用系统自带的 View 控件。 |
| 图片加载 | 沿用桌面版的原始图片 | 将按文件路径读取的方式改为按 Android 资源 ID 解码，使用独立预览验证装包。 |
| 游戏线程 | 沿用桌面版定时刷新思路 | 通过 Surface 生命周期驱动子线程，加入空画布判断和 finally 解锁，并等待旧线程结束后再允许创建新线程。 |
