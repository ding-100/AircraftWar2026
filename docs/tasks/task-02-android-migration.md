# 实验二 · 从桌面迁移到 Android

## 任务概述

使用 Java 创建 `AircraftWar2026`，支持 Android 11（API 30）及以上。以课程目录中的桌面版 `AircraftWar` 为迁移来源，按空壳、资源、游戏循环、交互与规则四步提交。

## 第一步 · 创建工程

在 Android Studio 中打开 `AircraftWar2026`，同步 Gradle 后运行 `app`。此步启动后会显示应用名。Gradle Wrapper 固定构建工具的版本，`app/build.gradle.kts` 指定 Android SDK 和 Java 版本。系统读取 `AndroidManifest.xml` 后找到启动 Activity，调用 `MainActivity.onCreate()`，再由 `setContentView()` 加载 XML 布局。

## 第二步 · 迁移资源

将桌面版的关卡背景、飞机、子弹、道具和护盾复制到 `res/drawable-nodpi`，驼峰命名改为小写加下划线。`nodpi` 让 Android 保留素材原始像素尺寸，渲染时统一缩放。Gradle 为资源生成 `R.drawable` 常量，`GameImages.decode()` 通过 `BitmapFactory.decodeResource()` 把资源解码成可绘制的 `Bitmap`。此步启动后能看到背景与英雄机的静态预览。

## AI 使用日志

| 环节 | 原本的思路 | AI 协助和实际采纳 |
| --- | --- | --- |
| 工程空壳 | 从 Empty Views Activity 起步 | 对照课程示例确认清单、启动 Activity 和布局的关系，保留 Java 与最低 API 30，采用系统自带的 View 控件。 |
| 图片加载 | 沿用桌面版的原始图片 | 将按文件路径读取的方式改为按 Android 资源 ID 解码，使用独立预览验证装包。 |
