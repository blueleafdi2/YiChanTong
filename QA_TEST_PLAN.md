# 遗产通 (Inheritance Law App) QA Test Plan

**Version:** 4.4.0  
**Platform:** Android  
**Document Date:** 2026-03-16  
**Language:** 中文 (Section Headers in English)

---

## 1. Document Overview

### 1.1 Purpose
本文档为遗产通 Android 继承法应用提供全面的质量保证测试计划，覆盖所有功能模块、边界条件、UI/UX 验证及 V4.1.0–V4.4.0 回归测试项。

### 1.2 Scope
- 首页、法律库、案例库、工具箱、我的、AI 聊天、全局搜索、法律词典、全局导航与法条链接
- V4.1.0 变更：AI 链接修复、对话持久化、搜索栏、工具法条链接、Word 导出
- V4.2.0 变更：AI 流式优化、搜索空状态、案例标签 ChipGroup、Word 中文字体、案例库扩展至 390 个
- V4.3.0 变更：法律词典（125 术语）、GlossaryActivity、术语高亮与弹窗、GlossaryHelper
- V4.3.1 变更：案例时间倒序、官方来源卡片与 WebView、sourceUrl 覆盖、Mine 版本号与数据源更新、AI 错误提示更新
- V4.4.0 变更：知识主题扩展至 25 个、FAQ 扩展至 40 个、司法解释增强（23 条法条关联继承编解释）

### 1.3 Test Environment
- Android 设备/模拟器（建议覆盖 API 21–34）
- 网络环境：WiFi、4G、弱网、离线
- 设备类型：手机、平板（如有适配）

---

## 2. Home Page (首页)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| HP-001 | Functional | 验证首页加载完整性 | 首页正常展示：搜索栏、6 个场景标签、AI 入口卡片、精选案例、8 个热门问题、知识主题 | P0 |
| HP-002 | Functional | 点击搜索栏 | 自动弹出键盘并进入搜索页面 | P0 |
| HP-003 | Functional | 点击 6 个场景标签（chips） | 每个标签可点击，跳转至对应场景内容或筛选结果 | P0 |
| HP-004 | Functional | 点击 AI 入口卡片 | 进入 AI 聊天页面，与「我的」中 AI 入口共享同一对话状态 | P0 |
| HP-005 | Functional | 点击精选案例 | 进入案例详情页，展示完整案例内容 | P0 |
| HP-006 | Functional | 点击 8 个热门问题（FAQ） | 每个 FAQ 可点击，展示对应答案或跳转至相关内容 | P0 |
| HP-007 | Functional | 点击知识主题 | 进入对应知识主题详情或列表 | P0 |
| HP-008 | UI/UX | 首页布局与适配 | 各元素正确排版，无遮挡、错位，不同屏幕尺寸适配正常 | P1 |
| HP-009 | UI/UX | 首页滚动流畅度 | 列表/内容区域滚动流畅，无卡顿 | P1 |
| HP-010 | Edge Case | 弱网/离线下首页加载 | 有合理加载态或错误提示，不崩溃 | P1 |
| HP-011 | Edge Case | 首次安装后首页展示 | 默认数据正确展示，无空白或异常占位 | P1 |

---

## 3. Law Library (法律库)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| LL-001 | Functional | 法律库入口与文章数量 | 法律库可正常进入，共 45 篇文章（第 1119–1163 条） | P0 |
| LL-002 | Functional | 按章节分组展示 | 文章按章节正确分组，章节标题清晰 | P0 |
| LL-003 | Functional | 单篇文章内容完整性 | 每篇文章包含：通俗解释、关键词（红色加粗）、生活示例、原文、司法解释、官方法律 HTML 查看器 | P0 |
| LL-004 | Functional | 关键词红色加粗显示 | 关键词在正文中以红色加粗正确高亮 | P0 |
| LL-005 | Functional | 官方法律 HTML 查看器 | HTML 查看器正确渲染法律原文，支持滚动、缩放 | P0 |
| LL-006 | Functional | 法律库内搜索/筛选 | 支持按关键词或章节筛选，结果准确 | P1 |
| LL-007 | Navigation | 从法律库返回 | 返回后保持列表滚动位置和展开状态 | P1 |
| LL-008 | Data Integrity | 法条编号连续性 | 1119–1163 条无缺失、无重复 | P0 |
| LL-009 | Edge Case | 点击法条内链接 | 若有内部引用，可正确跳转至对应法条 | P1 |
| LL-010 | Edge Case | 超长文章渲染 | 长文章完整展示，无截断或渲染异常 | P1 |
| LL-011 | UI/UX | 法律库列表加载 | 列表加载有加载态，无白屏闪烁 | P2 |

---

## 4. Case Library (案例库)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| CL-001 | Functional | 案例库入口与数量 | 案例库可正常进入，展示 390 个法院案例 | P0 |
| CL-002 | Functional | 按年份筛选 | 筛选器支持按年份筛选，结果正确 | P0 |
| CL-003 | Functional | 按地区筛选 | 筛选器支持按地区筛选，结果正确 | P0 |
| CL-004 | Functional | 按标签筛选 | 筛选器支持按标签筛选，结果正确 | P0 |
| CL-005 | Functional | 多条件组合筛选 | 年份+地区+标签组合筛选，结果符合逻辑 | P0 |
| CL-006 | Functional | 案例详情页展示 | 详情页展示完整案例内容，包含案号、法院、判决摘要等 | P0 |
| CL-007 | Functional | 案例详情内法条引用可点击 | 详情页中的法条引用（如【第xxx条】）可点击，跳转至对应法律条文 | P0 |
| CL-008 | Navigation | 从案例详情返回列表 | 返回后保持筛选条件和列表滚动位置 | P1 |
| CL-009 | Data Integrity | 案例数据完整性 | 每个案例至少包含案号、法院、裁判日期等必要字段 | P0 |
| CL-010 | Edge Case | 无筛选结果 | 筛选无结果时显示友好提示，不崩溃 | P1 |
| CL-011 | Edge Case | 案例详情内无效法条引用 | 无效引用不导致崩溃，有兜底处理 | P1 |
| CL-012 | UI/UX | 案例列表分页/加载更多 | 列表支持分页或无限滚动，加载流畅 | P1 |
| CL-013 | Functional | 案例时间倒序排列 | 案例列表默认按审判日期倒序（最新在前），2026-03 案例显示在最前 | P0 |
| CL-014 | Data Integrity | 无未来日期案例 | 所有案例 judgeDate 不超过当前日期（2026-03），不存在 2026-04 及之后的案例 | P0 |
| CL-015 | Functional | 官方来源卡片展示 | 案例详情底部显示「🔗 官方来源」卡片，含来源名称、案号、官方网址 | P0 |
| CL-016 | Functional | 查看官方来源 WebView | 点击「查看官方来源」按钮，进入 WebView 页面，显示案件信息+官网跳转按钮+温馨提示，无缩放问题 | P0 |
| CL-017 | Data Integrity | 5 种来源 URL 正确 | 中国裁判文书网→wenshu.court.gov.cn, 北大法宝→pkulaw.com, 人民法院案例库→rmfyalk.court.gov.cn, 中国法院网→chinacourt.org, 最高人民法院公报→gongbao.court.gov.cn | P0 |
| CL-018 | Data Integrity | sourceUrl 字段覆盖 | 所有 390 个案例均有 sourceUrl 字段 | P0 |
| CL-019 | Data Integrity | 案号与日期一致性 | 案号中的年份与 judgeDate 年份一致 | P0 |

---

## 5. Tools (工具箱)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| TL-001 | Functional | 工具箱入口与工具数量 | 工具箱可正常进入，顶部展示法律词典紫色卡片，下方展示 6 个工具 | P0 |
| TL-002 | Functional | 继承计算器 | 继承计算器可正常使用，输入合法数据后输出正确结果 | P0 |
| TL-003 | Functional | 遗嘱模板 | 遗嘱模板可正常填写、预览，支持 Word 导出 | P0 |
| TL-004 | Functional | 流程指南 | 流程指南内容完整，步骤清晰 | P0 |
| TL-005 | Functional | 清单模板 | 清单模板可正常填写、预览，支持 Word 导出 | P0 |
| TL-006 | Functional | 诉讼时效计算器 | 诉讼时效计算器输入日期后输出正确结果 | P0 |
| TL-007 | Functional | 术语表 | 术语表可浏览、搜索，内容准确 | P0 |
| TL-008 | Functional | 工具内法条引用可点击 | 各工具中的【第xxx条+关键词】可点击，跳转至对应法律条文 | P0 |
| TL-009 | Functional | Word 导出按钮 | 支持 Word 导出的工具（遗嘱模板、清单模板等）点击导出按钮可成功生成并导出 Word 文件 | P0 |
| TL-010 | Regression | V4.1.0 工具法条链接 | 所有工具内法条引用均通过 LawLinkHelper 正确链接，点击可跳转 | P0 |
| TL-011 | Regression | V4.1.0 Word 导出 | Word 导出功能正常，文件内容正确、格式可读 | P0 |
| TL-012 | Edge Case | 继承计算器非法输入 | 非法输入有校验提示，不崩溃 | P1 |
| TL-013 | Edge Case | 诉讼时效计算器边界日期 | 边界日期（如 1987 年前）计算正确或给出合理提示 | P1 |
| TL-014 | UI/UX | 工具页面布局 | 各工具页面布局合理，输入框、按钮可正常操作 | P1 |

---

## 6. Mine (我的)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| MN-001 | Functional | 我的页面入口 | 底部 Tab 或侧边栏可进入「我的」页面 | P0 |
| MN-002 | Functional | 收藏功能 | 可收藏法律、案例、工具等，收藏列表正确展示 | P0 |
| MN-003 | Functional | 笔记功能 | 可添加、编辑、删除笔记，笔记列表正确展示 | P0 |
| MN-004 | Functional | 阅读历史 | 阅读过的法律、案例正确记录并展示在阅读历史中 | P0 |
| MN-005 | Functional | AI 聊天入口 | 点击 AI 入口进入 AI 聊天，与首页 AI 入口共享同一对话状态 | P0 |
| MN-006 | Functional | 联系页面 | 联系页面展示微信二维码等信息，可长按保存或识别 | P0 |
| MN-007 | Data Integrity | 收藏/笔记/历史持久化 | 应用重启后收藏、笔记、阅读历史不丢失 | P0 |
| MN-008 | Edge Case | 空收藏/笔记/历史 | 无数据时显示空状态提示，不崩溃 | P1 |
| MN-009 | UI/UX | 我的页面布局 | 各入口清晰可点击，无遮挡 | P1 |
| MN-010 | Functional | 版本号和数据源更新 | 「我的」关于页面显示版本 4.4.0，不含硬编码案例数量，数据来源列表完整（6 个官网含域名） | P0 |
| MN-011 | Functional | AI 错误提示更新 | AI 余额不足时提示信息中案例数显示 390+ 而非 210+ | P0 |

---

## 7. AI Chat (AI 聊天)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| AC-001 | Functional | AI 聊天入口 | 从首页或「我的」进入 AI 聊天，展示对话界面 | P0 |
| AC-002 | Functional | 发送消息并获取回复 | 输入问题后发送，可收到 DeepSeek API 返回的回复 | P0 |
| AC-003 | Functional | 【第XXXX条】可点击 | 回复中的【第XXXX条】格式文本可点击，跳转至对应法律条文 | P0 |
| AC-004 | Functional | 【案例:关键词】可点击 | 回复中的【案例:关键词】格式文本可点击，跳转至对应案例 | P0 |
| AC-005 | Functional | 相关内容标签（法条+案例） | 回复下方展示相关法条、案例标签，点击可跳转 | P0 |
| AC-006 | Functional | 对话持久化 | 关闭应用后重新打开，历史对话仍存在（SharedPreferences 持久化） | P0 |
| AC-007 | Functional | 首页与我的 AI 入口共享状态 | 从首页进入 AI 聊天后，从「我的」进入看到相同对话；反之亦然 | P0 |
| AC-008 | Functional | 清除历史按钮 | 点击清除历史后，对话记录被清空，界面恢复初始状态 | P0 |
| AC-009 | Regression | V4.1.0 AI 链接修复 | 【第XXXX条】【案例:关键词】链接正确解析并跳转，无失效链接 | P0 |
| AC-010 | Regression | V4.1.0 对话持久化 | 对话在应用重启、进程被杀后仍可恢复 | P0 |
| AC-011 | Edge Case | 网络断开时发送消息 | 有网络错误提示，不崩溃，可重试 | P1 |
| AC-012 | Edge Case | API 超时 | 超时时有友好提示，可重试 | P1 |
| AC-013 | Edge Case | 空输入发送 | 空消息不允许发送或有提示 | P1 |
| AC-014 | Edge Case | 超长输入 | 超长输入有长度限制或提示 | P2 |
| AC-015 | UI/UX | 对话列表滚动 | 对话增多时滚动流畅，新消息自动滚动到底部 | P1 |

---

## 8. Search (搜索)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| SR-001 | Functional | 搜索入口与自动弹出键盘 | 点击搜索栏后自动弹出键盘，可直接输入 | P0 |
| SR-002 | Functional | 搜索法律 | 输入法律相关关键词，结果中包含匹配的法律条文 | P0 |
| SR-003 | Functional | 搜索案例 | 输入案例相关关键词，结果中包含匹配的案例 | P0 |
| SR-004 | Functional | 搜索 FAQ | 输入热门问题相关关键词，结果中包含匹配的 FAQ | P0 |
| SR-005 | Functional | 搜索知识主题 | 输入主题相关关键词，结果中包含匹配的知识主题 | P0 |
| SR-006 | Functional | 搜索工具 | 输入工具相关关键词，结果中包含匹配的工具 | P0 |
| SR-007 | Functional | 搜索术语表 | 输入术语相关关键词，结果中包含匹配的术语 | P0 |
| SR-008 | Functional | 结果类型标签 | 搜索结果展示类型标签（法律/案例/FAQ/主题/工具/术语），便于区分 | P0 |
| SR-009 | Regression | V4.1.0 搜索栏 | 首页搜索栏可正常点击并进入搜索，键盘自动弹出 | P0 |
| SR-010 | Edge Case | 空搜索 | 输入空字符串不触发无意义请求或崩溃 | P1 |
| SR-011 | Edge Case | 无结果 | 无匹配结果时显示友好提示 | P1 |
| SR-012 | Edge Case | 特殊字符搜索 | 输入特殊字符不崩溃，有合理处理 | P1 |
| SR-013 | UI/UX | 搜索联想/历史 | 若有搜索历史或联想，展示正确 | P2 |

---

## 9. 法律词典 (Legal Glossary)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| GL-101 | Functional | 法律词典入口（工具箱） | 工具箱顶部紫色卡片可点击进入法律词典 | P0 |
| GL-102 | Functional | 法律词典术语数量 | 共 125 个术语 | P0 |
| GL-103 | Functional | 全部分类筛选 | "全部" chip 选中时显示 125 个术语 | P0 |
| GL-104 | Functional | 小白必看筛选 | 过滤仅显示 medium + hard 难度（95 个） | P0 |
| GL-105 | Functional | 各分类筛选 | 每个分类 chip 过滤正确 | P0 |
| GL-106 | Functional | 搜索术语 | 输入关键词可搜索术语名、定义、举例、分类 | P0 |
| GL-107 | Functional | 搜索 + 分类联合筛选 | 搜索与分类 chip 可联合筛选，结果正确 | P0 |
| GL-108 | Functional | 点击术语卡片 | 弹出 MaterialAlertDialog 显示释义、通俗举例、相关法条、难度标签 | P0 |
| GL-109 | Functional | 难度标签正确 | easy=✅绿色, medium=📝橙色, hard=⚠️红色 | P0 |
| GL-110 | Functional | 法律详情中术语高亮 | medium/hard 术语在白话解读、生活案例、法律原文中显示紫色下划线 | P0 |
| GL-111 | Functional | 法律详情中术语可点击 | 点击紫色术语弹出 GlossaryHelper 对话框 | P0 |
| GL-112 | Functional | 案例详情标签关联词典 | 标签匹配词典术语时显示 ? 图标 | P0 |
| GL-113 | Functional | 案例详情标签点击 | 点击有 ? 图标的标签弹出术语解释 | P0 |
| GL-114 | Functional | 搜索结果中术语展示 | 搜索结果中术语显示难度 emoji 和分类标签 | P1 |
| GL-115 | Data Integrity | 术语举例完整性 | 抽查 10 个 hard 术语，均有具体生活举例 | P1 |
| GL-116 | Edge Case | 空搜索 | 搜索框无输入显示全部术语 | P1 |
| GL-117 | Edge Case | 无结果 | 搜索不存在的内容显示空列表 | P1 |

---

## 10. Global Features (全局功能)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| GL-001 | Functional | 全局法条链接 LawLinkHelper | 法律库、案例库、工具箱、AI 聊天中的法条引用均通过 LawLinkHelper 统一处理，可点击跳转 | P0 |
| GL-002 | Functional | 法条链接带关键词 | 点击法条链接时，若有关键词，跳转后正确高亮或定位到关键词 | P0 |
| GL-003 | Navigation | 返回键保持状态 | 从详情页返回列表时，列表滚动位置、筛选条件、展开状态得以保持 | P0 |
| GL-004 | Navigation | 多级返回 | 多级页面返回时，每级状态正确恢复 | P1 |
| GL-005 | Navigation | 系统返回键 | 系统返回键与应用内返回按钮行为一致 | P1 |
| GL-006 | Performance | 冷启动时间 | 应用冷启动在 3 秒内完成首屏展示（可依设备调整） | P1 |
| GL-007 | Performance | 内存占用 | 长时间使用无明显内存泄漏，内存占用在合理范围 | P1 |
| GL-008 | Performance | 列表滚动性能 | 长列表（法律库、案例库）滚动流畅，无卡顿 | P1 |
| GL-009 | Edge Case | 横竖屏切换 | 横竖屏切换时布局正确，状态不丢失 | P1 |
| GL-010 | Edge Case | 应用切后台再恢复 | 切后台一段时间后恢复，界面状态正确，无白屏 | P1 |
| GL-011 | Edge Case | 低存储空间 | 低存储空间下 Word 导出等操作有合理提示 | P2 |

---

## 11. V4.4.0 Content Enhancement

### 11.1 Knowledge Topics (知识主题)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| TC-NEW-01 | Functional | 验证 25 个主题加载正确 | 知识主题从 20 个扩展至 25 个，全部正确加载展示 | P0 |
| TC-NEW-02 | Functional | topic_21（遗嘱信托）内容完整性 | topic_21 遗嘱信托包含完整内容，含《信托法》引用 | P0 |
| TC-NEW-03 | Functional | topic_22（民法典变化）对比展示 | topic_22 民法典变化展示旧法 vs 新法对比 | P0 |
| TC-NEW-04 | Functional | topic_23（保险金养老金）分类表 | topic_23 保险金养老金有清晰的 是/否遗产 分类表 | P0 |
| TC-NEW-05 | Functional | topic_24（纠纷解决）决策树 | topic_24 纠纷解决包含决策树路径 | P0 |
| TC-NEW-06 | Functional | topic_25（多子女）分配规则 | topic_25 多子女包含分配例外规则 | P0 |

### 11.2 FAQs (热门问题)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| TC-NEW-07 | Functional | 验证 40 个 FAQ 加载正确 | FAQ 从 30 个扩展至 40 个，全部正确加载展示 | P0 |
| TC-NEW-08 | Functional | faq_31 至 faq_40 字段完整性 | 新增 faq_31 至 faq_40 均包含 question、answer、relatedLaws、tags | P0 |
| TC-NEW-09 | Functional | 新 FAQ 搜索可检索 | 新增 FAQ 可在搜索中正确返回结果 | P0 |
| TC-NEW-10 | Functional | 新 FAQ 关联主题链接 | 新增 FAQ 可正确链接至相关主题 | P0 |

### 11.3 Judicial Interpretation Enhancement (司法解释增强)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| TC-NEW-11 | Functional | 第 1121 条司法解释展示 | 第 1121 条显示【继承编解释（一）第1-4条】 | P0 |
| TC-NEW-12 | Functional | 第 1125 条司法解释展示 | 第 1125 条显示【继承编解释（一）第5-9条】 | P0 |
| TC-NEW-13 | Functional | 第 1142 条司法解释展示 | 第 1142 条显示【继承编解释（一）第38条】 | P0 |
| TC-NEW-14 | Functional | 23 条增强法条展示 | 全部 23 条增强法条均显示具体条文编号 | P0 |

---

## 12. Regression Test Suite (V4.1.0–V4.4.0)

| Test ID | Category | Test Description | Expected Result | Priority |
|---------|----------|-------------------|-----------------|----------|
| RG-001 | Regression | AI 链接修复 | AI 回复中【第XXXX条】【案例:关键词】均可正确点击并跳转 | P0 |
| RG-002 | Regression | 对话持久化 | AI 对话通过 SharedPreferences 持久化，重启后可恢复 | P0 |
| RG-003 | Regression | 首页搜索栏 | 首页搜索栏可点击，自动弹出键盘，进入搜索流程 | P0 |
| RG-004 | Regression | 工具法条链接 | 工具箱 6 个工具内所有法条引用可点击，跳转正确 | P0 |
| RG-005 | Regression | Word 导出 | 遗嘱模板、清单模板等 Word 导出功能正常，文件可打开 | P0 |
| RG-006 | Regression | 全局 LawLinkHelper | 全应用法条链接统一使用 LawLinkHelper，无遗漏或失效 | P0 |
| RG-007 | Regression | AI 流式响应无卡顿 | AI 两阶段响应（先流式文本，后异步相关内容）无卡顿，体验流畅 | P0 |
| RG-008 | Regression | AI 异步相关内容正确显示 | AI 流式文本结束后，相关法条、案例标签正确异步加载并展示 | P0 |
| RG-009 | Regression | AI 非相关问题处理 | 用户提问与继承法无关时，graceful 处理，展示免责声明 | P0 |
| RG-010 | Regression | 搜索空结果状态 + AI 跳转 | 搜索无结果时显示「未找到「xxx」」，按钮可跳转至 AI 助手 | P0 |
| RG-011 | Regression | 案例标签 ChipGroup 自动换行 | 案例详情页标签由 LinearLayout 改为 ChipGroup，支持正确换行 | P0 |
| RG-012 | Regression | Word 导出中文字体兼容 | Word 导出中文字体兼容性优化，字体显示正确 | P0 |
| RG-013 | Regression | 390 个案例筛选功能正常 | 案例库扩展至 390 个后，筛选功能正常，结果正确 | P0 |
| RG-014 | Regression | 数字货币/加密货币案例可检索 | 新增数字货币、加密货币相关案例可被正确检索 | P0 |
| RG-015 | Regression | 法律词典术语弹窗全流程 | 法律库、案例库、工具箱中术语点击可弹出 GlossaryHelper 对话框 | P0 |
| RG-016 | Regression | 法律库术语高亮一致性 | 法律详情中 medium/hard 术语在白话解读、生活案例、法律原文中显示紫色下划线 | P0 |
| RG-017 | Regression | 案例库标签词典关联 | 案例详情标签匹配词典术语时显示 ? 图标，点击可弹出术语解释 | P0 |
| RG-018 | Regression | V4.3.1 案例倒序排列 | 案例库列表按时间倒序显示 | P0 |
| RG-019 | Regression | V4.3.1 无未来日期案例 | 无 2026-04 及之后日期的案例 | P0 |
| RG-020 | Regression | V4.3.1 官方来源 WebView | 案例详情「查看官方来源」功能正常 | P0 |
| RG-021 | Regression | V4.3.1 Mine 页面无硬编码案例数 | 「我的」关于页面不含「210+」 | P0 |
| RG-022 | Regression | V4.3.1 法律词典无 ANR | 法律词典功能正常使用无卡顿 | P0 |
| RG-023 | Regression | V4.3.1 案例标签词典关联 | 标签匹配词典术语时显示 ? 图标并可点击 | P0 |
| RG-024 | Regression | V4.3.1 法律详情术语高亮 | 仅 hard 难度术语高亮，无 ANR | P0 |
| TC-REG-NEW-01 | Regression | V4.4.0 原有 20 个主题正常 | 原有 20 个知识主题仍正确加载并展示 | P0 |
| TC-REG-NEW-02 | Regression | V4.4.0 原有 30 个 FAQ 正常 | 原有 30 个 FAQ 仍正确加载并展示 | P0 |
| TC-REG-NEW-03 | Regression | V4.4.0 搜索全内容类型 | 搜索仍可返回所有内容类型的结果 | P0 |
| TC-REG-NEW-04 | Regression | V4.4.0 AI 助手内容数量 | AI 助手引用更新后的内容数量（25 主题、40 FAQ） | P0 |

---

## 13. Test Case Summary

| Category | P0 | P1 | P2 | Total |
|----------|----|----|----|-------|
| Home Page | 5 | 5 | 0 | 11 |
| Law Library | 4 | 5 | 1 | 11 |
| Case Library | 15 | 4 | 0 | 19 |
| Tools | 8 | 4 | 0 | 14 |
| Mine | 8 | 2 | 0 | 10 |
| AI Chat | 8 | 4 | 1 | 15 |
| Search | 8 | 3 | 1 | 13 |
| Legal Glossary | 14 | 3 | 0 | 17 |
| Global | 3 | 6 | 1 | 11 |
| V4.4.0 Content Enhancement | 14 | 0 | 0 | 14 |
| Regression | 28 | 0 | 0 | 28 |
| **Total** | **114** | **36** | **4** | **154** |

---

## 14. Test Execution Notes

### 14.1 Priority Definitions
- **P0**: 核心功能，阻塞发布，必须全部通过
- **P1**: 重要功能，建议全部通过后再发布
- **P2**: 增强体验，可酌情延后

### 14.2 Recommended Execution Order
1. P0 回归用例（RG-001 ~ RG-024, TC-REG-NEW-01 ~ TC-REG-NEW-04）
2. V4.4.0 内容增强用例（TC-NEW-01 ~ TC-NEW-14）
3. 各模块 P0 功能用例
4. 全局 P0 用例（GL-001 ~ GL-003）
5. P1 用例
6. P2 用例

### 14.3 Defect Severity Mapping
- 功能无法使用 → Critical
- 功能异常但可绕过 → Major
- 体验问题、边界异常 → Minor
- 文案、样式小问题 → Trivial

---

## 15. Sign-off

| Role | Name | Date | Signature |
|------|------|------|------------|
| QA Lead | | | |
| Dev Lead | | | |
| Product | | | |

---

*Document Version: 1.3*  
*Last Updated: 2026-03-16*
