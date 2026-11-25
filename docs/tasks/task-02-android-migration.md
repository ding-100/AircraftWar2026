# 实验二 · 从桌面迁移到 Android

## 任务概述

使用 Java 创建 `AircraftWar2026`，支持 Android 11（API 30）及以上。以课程目录中的桌面版 `AircraftWar` 为迁移来源，按空壳、资源、游戏循环、交互与规则四步提交。

## 第一步 · 创建工程

在 Android Studio 中打开 `AircraftWar2026`，同步 Gradle 后运行 `app`。此步启动后会显示应用名。Gradle Wrapper 固定构建工具的版本，`app/build.gradle.kts` 指定 Android SDK 和 Java 版本。系统读取 `AndroidManifest.xml` 后找到启动 Activity，调用 `MainActivity.onCreate()`，再由 `setContentView()` 加载 XML 布局。

## AI 使用日志

| 环节 | 原本的思路 | AI 协助和实际采纳 |
| --- | --- | --- |
| 工程空壳 | 从 Empty Views Activity 起步 | 对照课程示例确认清单、启动 Activity 和布局的关系，保留 Java 与最低 API 30，采用系统自带的 View 控件。 |
