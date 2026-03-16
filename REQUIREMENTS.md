# 遗产通 (YiChanTong) — Product Requirements Document

## 1. Product Vision

### App Name
**遗产通** (YiChanTong)

### Target Users
- **法律小白**：无法律背景的普通用户，需要通俗易懂的继承法知识
- **法律专业人士**：律师、法务、法律研究者，需要快速查阅法条、案例和术语

### Core Value Proposition
遗产通致力于让每一位中国公民都能**看得懂、查得到、用得上**遗产继承相关法律知识。通过整合《民法典》继承编全文解读、全国真实判例、法律词典、实用工具和 AI 法律助手，为用户提供一站式继承法学习与查询平台。

---

## 2. Feature Requirements (by Module)

### 2.1 首页 (Home)

| Feature | Description | Acceptance Criteria |
|---------|-------------|---------------------|
| Search bar | 顶部搜索入口 | 点击跳转至 SearchActivity |
| 6 scenario tags | 场景化快捷入口 | 点击跳转至 TopicDetailActivity，对应 topic_01, topic_02, topic_01, topic_10, topic_09, topic_11 |
| AI assistant entry | AI 法律助手入口卡片 | 点击打开 AIActivity |
| Featured case | 每日推荐案例 | 基于日期轮换展示，优先多标签复杂案例，点击打开 CaseDetailActivity |
| 8 FAQ items | 热门问题列表 | 按预设顺序展示（faq_01, faq_07, faq_03 等），共 40 个 FAQ，点击跳转 TopicDetailActivity |
| Knowledge topics | 知识专题列表 | 25 个知识专题，首页展示前 8 个，点击跳转 TopicDetailActivity |
| Law references | 法条引用 | 所有法条引用可点击，跳转 LawDetailActivity |

**Scenario Tags:**
- 亲人去世后遗产怎么分
- 我想立一份有效遗嘱
- 遗产分配有争议怎么办
- 房产继承过户流程
- 父母的债务要子女还吗
- 数字遗产怎么继承

---

### 2.2 法律库 (Law Library)

| Feature | Description | Acceptance Criteria |
|---------|-------------|---------------------|
| Law articles | 民法典继承编全文 | 45 条（第 1119–1163 条），按章节分组展示 |
| Chapter grouping | 章节分组 | 按 chapter 字段分组，支持章节导航 |
| Keyword highlighting | 关键词高亮 | 法条关键词以红色加粗显示 |
| Glossary term highlighting | 术语高亮 | 晦涩术语（difficulty=hard）紫色下划线，可点击弹出释义 |
| Official law HTML viewer | 官方原文查看 | 点击「查看官方原文」打开 WebViewActivity，展示 flk.npc.gov.cn 风格 HTML |
| Favorites | 收藏功能 | 支持收藏/取消收藏，数据持久化 |
| Notes | 笔记功能 | 支持为法条添加笔记，数据持久化 |

**Law Article Fields:**
- id, article, chapter, title, originalText, plainExplanation, lifeExample, judicialInterpretation, legislativeHistory, keywords
- 23 条法条含《继承编解释（一）》具体条文编号引用

---

### 2.3 案例库 (Case Library)

| Feature | Description | Acceptance Criteria |
|---------|-------------|---------------------|
| Case list | 案例列表 | 390 件案例，按审判日期倒序 |
| Filters | 多维度筛选 | 年份、省份、标签、案件类型（ChipGroup） |
| Dynamic count | 筛选结果计数 | 显示「共 X 件」 |
| Case detail | 案例详情 | 标题、案号、法院、日期、摘要、判决、法律依据、裁判要旨 |
| Tags with glossary | 标签与术语 | 标签若为词典术语，显示 ? 图标，点击弹出释义 |
| Law links | 法律依据链接 | 法律依据列表可点击，跳转 LawDetailActivity |
| Source card | 来源卡片 | 展示来源、案号、网址 |
| Official source WebView | 官方来源查看 | 点击打开 WebViewActivity，含「前往官网」「搜索本案裁判文书」按钮 |
| Favorites & Notes | 收藏与笔记 | 支持收藏、添加笔记 |

**Filter Types:**
- 年份（Spinner）
- 省份（Spinner）
- 标签（Spinner）
- 案件类型（ChipGroup）

---

### 2.4 工具箱 (Toolbox)

| Feature | Description | Acceptance Criteria |
|---------|-------------|---------------------|
| 法律词典 card | 顶部紫色卡片 | 点击跳转 GlossaryActivity |
| 6 tools | 实用工具列表 | 继承计算器、遗嘱模板、流程指引、遗产清单、诉讼时效计算器、法律术语词典 |
| Word export | Word 导出 | 支持导出 .doc，含中文字体兼容（Microsoft YaHei, PingFang SC 等） |
| Law link references | 法条引用 | 工具内容中法条引用可点击，跳转 LawDetailActivity |

**Tools:**
1. 继承关系计算器 (inheritance_calculator)
2. 遗嘱模板生成器 (will_template_generator)
3. 继承流程指引 (inheritance_process_guide)
4. 遗产清单模板 (estate_inventory_template)
5. 诉讼时效计算器 (limitation_calculator)
6. 法律术语词典 (legal_glossary)

---

### 2.5 法律词典 (Legal Glossary)

| Feature | Description | Acceptance Criteria |
|---------|-------------|---------------------|
| Term list | 术语列表 | 125 个术语 |
| Difficulty levels | 难度分级 | easy / medium / hard（✅ 基础、📝 进阶、⚠️ 高级） |
| Categories | 分类 | 11 个分类 |
| Search | 搜索 | 支持按术语、释义、举例搜索 |
| Category filter | 分类筛选 | ChipGroup 筛选，含「全部」「⚠️ 小白必看」 |
| Difficulty filter | 难度筛选 | 「小白必看」= medium + hard |
| Popup dialog | 术语弹窗 | 释义、通俗举例、相关法条 |
| Integration | 集成展示 | 法律详情页紫色下划线；案例标签 ? 图标 |

---

### 2.6 我的 (Mine)

| Feature | Description | Acceptance Criteria |
|---------|-------------|---------------------|
| AI assistant entry | AI 入口 | 与首页共享状态，点击打开 AIActivity |
| Favorites | 我的收藏 | 跳转 FavoritesActivity，展示收藏的法条、案例 |
| Notes | 我的笔记 | 跳转 NotesActivity，展示笔记列表 |
| Reading History | 阅读历史 | 跳转 HistoryActivity，展示最近浏览 |
| Contact | 联系法律专家 | 跳转 ContactActivity：12348 热线、微信二维码、复制微信号 |
| About | 关于 | 版本号、数据来源、隐私政策链接 |

**Contact Page:**
- 拨打 12348 免费法律热线
- 微信二维码展示与保存到相册
- 复制微信号 LawServicePro

---

### 2.7 AI Chat

| Feature | Description | Acceptance Criteria |
|---------|-------------|---------------------|
| DeepSeek API | AI 接口 | 使用 DeepSeek API 流式响应 |
| Streaming | 流式输出 | 实时显示 AI 回复，120ms 节流更新 |
| Two-phase response | 两阶段响应 | 先流式输出文本，再异步解析并展示相关内容卡片 |
| Law link clickable | 法条可点击 | 【第 XXXX 条】格式可点击，跳转 LawDetailActivity |
| Case link clickable | 案例可点击 | 【案例:关键词】格式可点击，跳转 CaseDetailActivity |
| Conversation persistence | 对话持久化 | SharedPreferences 保存，最多 100 条 |
| Off-topic handling | 非继承问题 | 友好回复并提示专注遗产继承领域 |

**System Prompt 要点:**
- 基于《民法典》继承编第 1119–1163 条
- 必须引用具体法条编号
- 可关联案例时使用【案例:关键词】标记
- 回答控制在 500 字以内

---

### 2.8 Search

| Feature | Description | Acceptance Criteria |
|---------|-------------|---------------------|
| Full-text search | 全文搜索 | 覆盖法律、案例、FAQ、专题、工具、术语 |
| Empty state | 无结果状态 | 展示「未找到」提示 + AI 助手入口，可携带搜索词跳转 |
| Result type badges | 结果类型标签 | 法条/案例/问答/专题/工具/术语 不同颜色徽章 |
| Min query length | 最小长度 | 至少 2 字符触发搜索 |

**Search Scope:**
- Laws: title, originalText, plainExplanation, keywords
- Cases: title, caseSummary, court, tags
- FAQs: question, answer
- Topics: title, description
- Tools: title, description, content
- Glossary: term, definition, example, category

---

## 3. Non-Functional Requirements

### Performance
- 法律、案例、术语等 JSON 采用懒加载，DataProvider 单例缓存
- AI 流式响应：120ms 节流更新，直接 TextView.setText 避免 RecyclerView 全量 rebind
- 术语高亮仅处理 hard 难度术语，首次出现

### Compatibility
- minSdk 24, targetSdk 36
- Java 17

### Data Quality
- 法条、案例、术语数据来源于官方或权威渠道
- 法条覆盖《民法典》继承编全文（第 1119–1163 条）

### Offline Capability
- 法律、案例、术语、工具、专题、FAQ 均以 JSON 打包在 assets，无需网络即可浏览
- AI 功能需联网

---

## 4. Data Sources

| Source | URL | Description |
|--------|-----|-------------|
| 全国人民代表大会 | https://flk.npc.gov.cn/ | 法律原文 |
| 中国裁判文书网 | https://wenshu.court.gov.cn | 裁判文书 |
| 人民法院案例库 | https://rmfyalk.court.gov.cn | 典型案例 |
| 最高人民法院公报 | https://gongbao.court.gov.cn | 公报案例 |
| 北大法宝 | https://pkulaw.com | 法律检索 |
| 中国法院网 | https://chinacourt.org | 法院资讯 |

---

## 5. Version History

| Version | Key Changes |
|---------|-------------|
| V1.0 | 基础法律库、案例库、知识专题、FAQ |
| V2.x | 工具箱、法律词典、收藏与笔记 |
| V3.x | AI 法律助手（DeepSeek）、搜索、阅读历史 |
| V4.0 | 术语高亮、法条链接、案例标签术语集成 |
| V4.1 | 官方来源 WebView、搜索按钮、移动端适配 |
| V4.2 | Word 导出、中文字体兼容、联系页 |
| V4.3.0+ | 125 术语、390 案例、45 法条、6 工具、隐私政策、数据来源说明 |
| V4.4.0 | **新增 5 个知识专题**（topic_21–25）：遗嘱信托实务指南、民法典继承编重大变化解读、保险金养老金与遗产继承、遗产继承纠纷解决路径、多子女家庭遗产分配实务；**新增 10 个 FAQ**（faq_31–40）：遗嘱信托设立、保险金是否属于遗产、养老金/住房公积金继承、死亡赔偿金分配、民法典重大变化、彩礼嫁妆继承、调解 vs 诉讼选择、父母偏心问题、录音录像遗嘱有效性、法律援助渠道；**23 条法条增强**：补充《继承编解释（一）》具体条文编号；**内容总量**：45 法条、25 专题、40 FAQ、125 术语、390 案例 |
