# 用户指令记忆

本文件记录了用户的指令、偏好和教导，用于在未来的交互中提供参考。

## 格式

### 用户指令条目
用户指令条目应遵循以下格式：

[用户指令摘要]
- Date: [YYYY-MM-DD]
- Context: [提及的场景或时间]
- Instructions:
  - [用户教导或指示的内容，逐行描述]

### 项目知识条目
Agent 在任务执行过程中发现的条目应遵循以下格式：

[项目知识摘要]
- Date: [YYYY-MM-DD]
- Context: Agent 在执行 [具体任务描述] 时发现
- Category: [代码结构|代码模式|代码生成|构建方法|测试方法|依赖关系|环境配置]
- Instructions:
  - [具体的知识点，逐行描述]

## 去重策略
- 添加新条目前，检查是否存在相似或相同的指令
- 若发现重复，跳过新条目或与已有条目合并
- 合并时，更新上下文或日期信息
- 这有助于避免冗余条目，保持记忆文件整洁

## 条目

[项目规格文档位置约定]
- Date: 2026-04-06
- Context: Agent 在执行“根据设计报告完成第一阶段开发”时发现
- Category: 代码结构
- Instructions:
  - 当前项目的设计文档位于 `.monkeycode/specs/2026-04-06-emby-tv-player/design.md`
  - 该规格定义了分层架构：UI / Domain / Data / Network / Playback

[测试回归优先级偏好]
- Date: 2026-04-06
- Context: 用户在进入第三阶段前提出“先补第二阶段回归测试”
- Instructions:
  - 在推进下一阶段功能前，优先补充当前阶段的回归测试
  - 当前项目第二阶段测试范围优先包含 ViewModel 单测和基础 UI 测试

[SMB 协议实现依赖]
- Date: 2026-04-06
- Context: Agent 在执行“第三阶段 SMB 浏览器接入”时发现
- Category: 依赖关系
- Instructions:
  - SMB 浏览器使用 `eu.agno3.jcifs:jcifs-ng:2.1.10` 作为 SMB v2/v3 客户端依赖
  - `Settings` 页面已接入 SMB 浏览器入口，作为本地网络存储浏览主入口

[SMB 播放策略]
- Date: 2026-04-06
- Context: Agent 在执行“SMB 文件直连播放”时发现
- Category: 代码模式
- Instructions:
  - SMB 列表中文件项可直接触发播放，目录项保持进入目录
  - SMB 文件播放跳转 Player 时禁用 Emby 播放进度回传
