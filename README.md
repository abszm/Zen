# Emby TV Player

Android TV 客户端第一阶段 MVP 工程。

## 已实现

- Kotlin + Jetpack Compose for TV 基础工程
- D-Pad 左右键控制的隐藏式侧边栏
- Emby 第二阶段能力：登录、媒体库拉取、播放页联动
- 第二阶段完善：登录错误细分提示、媒体库搜索与分页、播放失败重试
- 第三阶段进展：SMB 浏览器（连接、目录浏览、回退与刷新）
- 第三阶段进展：SMB 文件直连播放（从 SMB 列表直接跳转 Player）
- Home / Library / Player / Settings 页面路由
- 分层目录骨架（UI / Domain / Data）

## 本地运行

在 Android Studio 中直接打开仓库并同步 Gradle，然后运行 `app` 模块到 Android TV 模拟器或设备。
