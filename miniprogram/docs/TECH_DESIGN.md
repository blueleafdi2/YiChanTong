# 遗产通微信小程序 — 技术架构设计文档

| 文档信息 | 内容 |
|---------|------|
| 工程路径 | `.../app/law/miniprogram` |
| 框架 | 微信小程序原生（MINA） |
| 文档类型 | 技术设计（TDD） |
| 关联文档 | `docs/REQUIREMENTS.md` |

---

## 1. 技术栈

| 层级 | 选型 | 说明 |
|------|------|------|
| 运行时 | 微信客户端 + 小程序基础库 | `project.config.json` 中 `libVersion` 指定开发基准（如 3.6.0） |
| 框架 | **MINA**（微信官方） | 逻辑层 JS + 视图层 WXML/WXSS + 配置 JSON |
| 视图 | **WXML** + **WXSS** | 组件化页面；`style: v2` 启用新版组件样式 |
| 逻辑 | **JavaScript**（ES6） | `require` 模块化；无 TypeScript 时为纯 JS |
| 数据 | 本地 **JSON** | 构建期打包进包体；案例大文件放分包 |
| AI | **DeepSeek** Chat Completions HTTP API | `wx.request` 调用；**生产环境须走后端代理** |
| 埋点 | 本地 Storage + `wx.reportEvent` | 见 `utils/analytics.js` |

---

## 2. 项目结构

以下为仓库内主要目录与文件职责（`private.key` 等敏感文件不应提交公共仓库，仅本地忽略配置）。

```
miniprogram/
├── app.js                 # 全局入口：数据加载、search/getLawById、loadCases
├── app.json               # 页面路由、tabBar、分包、preloadRule、lazyCodeLoading
├── app.wxss               # 全局样式变量与通用 class
├── project.config.json    # AppID、编译选项、packOptions.ignore
├── sitemap.json           # 索引配置
│
├── pages/                 # 【主包】Tab 页 + 全局搜索
│   ├── index/             # 首页
│   ├── laws/              # 法律库列表
│   ├── cases/             # 案例库列表（触发 loadCases）
│   ├── tools/             # 工具箱入口
│   ├── mine/              # 我的（含隐私弹窗）
│   └── search/            # 全局搜索（debounce）
│
├── subpkg/                # 【分包】详情与大体积数据
│   ├── data/
│   │   └── cases.json     # 390 条案例（大文件，与主包隔离）
│   ├── law-detail/        # 法条详情
│   ├── case-detail/       # 案例详情
│   ├── topic-detail/      # 专题详情
│   ├── tool-detail/       # 工具详情（含法条块解析、导出复制）
│   ├── glossary/          # 法律词典
│   ├── ai/                # AI 助手（DeepSeek、会话存储、案例关联）
│   ├── favorites/         # 收藏列表
│   ├── notes/             # 笔记
│   ├── history/           # 浏览历史
│   └── contact/           # 咨询专家/联系
│
├── data/                  # 【主包】小型数据集
│   ├── laws.json          # 45 条法条
│   ├── faq.json
│   ├── topics.json
│   ├── glossary.json
│   └── tools.json
│
├── utils/
│   ├── analytics.js       # 埋点：计数、环形日志、wx.reportEvent、exportReport
│   ├── favorites.js       # 收藏 / 笔记 / 历史的 Storage 封装
│   └── law-link.js        # 法条 ID 提取、正文分片、中文数字映射、跳转
│
└── assets/                # Tab 图标等静态资源
```

### 2.1 页面与分包对应关系

```
┌──────────────────────────────────────────────────────────────┐
│                        app.json                               │
│  pages[]: tab + search  │  subpackages.root: subpkg/          │
└──────────────────────────────────────────────────────────────┘
         │                              │
         ▼                              ▼
   主包：首屏、Tab 导航               分包：详情、AI、收藏等
```

---

## 3. 分包策略

### 3.1 设计目标

| 包 | 内容 | 体积目标（经验值） |
|----|------|-------------------|
| **主包** | 5 个 Tab 页 + `pages/search` + `utils` + `data/*.json`（除 cases）+ 公共样式与图片 | **约 ≤ 800KB**（压缩后依微信分析为准） |
| **子包 subpkg** | 全部 `subpkg/*` 页面 + `subpkg/data/cases.json` | **约 ≤ 600KB**（案例 JSON 为主） |

> 实际上传前以微信开发者工具「代码依赖分析」与体验版分包大小为准，上表为设计指引。

### 3.2 `app.json` 配置摘要

- **`pages`**：主包页面列表，第一项为小程序入口页。
- **`subpackages`**：`root: subpkg`，下列 `law-detail`、`case-detail`、`ai` 等。
- **`lazyCodeLoading`: `"requiredComponents"`**：按需注入自定义组件相关代码，优化启动。

### 3.3 `preloadRule` 说明

```json
"preloadRule": {
  "pages/index/index": {
    "network": "all",
    "packages": ["subpkg"]
  }
}
```

| 字段 | 含义 |
|------|------|
| `pages/index/index` | 用户进入首页后触发预下载规则 |
| `network: all` | Wi‑Fi 与蜂窝网络均允许预下载（可按产品策略改为 `wifi`） |
| `packages: ["subpkg"]` | 预拉取名为分包根目录标识的包（此处为 `subpkg` 分包） |

**效果**：用户在首页停留时，微信客户端在后台下载子包资源，降低首次打开法条/案例/AI 页的等待时间。

---

## 4. 数据架构

### 4.1 六类 JSON 与 Schema 说明

以下字段为逻辑 Schema，用于研发对齐与联调；具体以仓库 JSON 为准。

#### 4.1.1 `laws.json` — 法条

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 条号数字串，如 `"1127"`，与继承编对应 |
| `article` | string | 中文条名，如「第一千一百二十七条」 |
| `chapter` | string | 章节标题，用于列表分组 |
| `title` | string | 条文短标题 |
| `originalText` | string | 官方原文 |
| `plainExplanation` | string | 白话解释 |
| `lifeExample` | string | 生活化例子（可选） |
| `judicialInterpretation` | string | 司法解释要点（可选） |
| `legislativeHistory` | string | 立法沿革（可选） |
| `keywords` | string[] | 关键词，用于搜索与高亮 |

#### 4.1.2 `cases.json` — 案例

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 案例唯一标识 |
| `title` | string | 案例标题 |
| `court` | string | 审理法院 |
| `province` | string | 省份（筛选） |
| `judgeDate` | string | 裁判日期，建议 `YYYY-MM-DD` |
| `caseType` | string | 案件类型（筛选） |
| `tags` | string[] | 标签云来源 |
| `caseSummary` / 正文类字段 | string | 摘要与详情（以实际字段名为准） |
| `legalBasis` | string[] | 法律依据列表，供跳转法条解析 |

#### 4.1.3 `faq.json`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | FAQ ID |
| `question` | string | 问题 |
| `answer` | string | 回答全文 |
| 关联专题 | string | 若存在 `topicId` 等字段，用于首页跳转 |

#### 4.1.4 `topics.json` — 专题

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 专题 ID，如 `topic_01` |
| `title` | string | 标题 |
| `description` | string | 摘要 |
| `content` | string | 正文（可被搜索索引） |

#### 4.1.5 `glossary.json` — 词典

| 字段 | 类型 | 说明 |
|------|------|------|
| `term` | string | 术语名 |
| `definition` | string | 释义 |
| `category` | string | 分类 Tab |
| `difficulty` | string | `easy` / `medium` / `hard`，「小白必看」筛选用 |

#### 4.1.6 `tools.json` — 工具

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 工具 ID |
| `title` | string | 名称 |
| `content` | string | Markdown 风格正文（`#` 标题行由 `tool-detail` 解析） |

### 4.2 `App.globalData` 存储策略

```javascript
// 逻辑示意（见 app.js）
globalData: {
  laws: [],        // onLaunch require laws.json
  topics: [],      // require topics.json
  faq: [],         // require faq.json
  glossary: [],    // require glossary.json
  tools: [],       // require tools.json
  cases: [],       // 延迟 require subpkg/data/cases.json
  casesLoaded: false,
  dataReady: false // 主包五件套是否就绪
}
```

| 数据 | 加载时机 | 原因 |
|------|----------|------|
| laws/topics/faq/glossary/tools | `onLaunch` → `loadData()` | 体量较小，支撑 Tab 与搜索主路径 |
| cases | 首次 `loadCases()` | 体积大，避免拖慢冷启动；进入案例 Tab、首页刷新、AI 页等调用 |

### 4.3 `cases` 懒加载机制

```
用户打开小程序
      │
      ▼
loadData() ──► laws, topics, faq, glossary, tools 进入内存
      │
      ├─► 用户仅浏览法律库：cases 可不加载（搜索案例除外）
      │
      └─► 用户进入 cases / index onShow / search onLoad / ai onLoad
              │
              ▼
          loadCases()
              │
              ▼
          require('subpkg/data/cases.json')
              │
              ▼
          casesLoaded = true，全局 search 可匹配案例
```

**注意**：`app.search` 内对案例的遍历受 `casesLoaded` 守卫；若用户在未加载案例前搜索，结果可能无法包含案例类型，需在 PRD 中已通过「进入相关页触发加载」或搜索页 `onLoad` 调用 `loadCases()` 缓解。

---

## 5. 核心模块设计

### 5.1 DataProvider（`app.js`）

职责：

- **数据装载**：`loadData`、`loadCases`
- **检索**：`search(query)` — 小写化 query，长度由调用方控制（页面层 ≥2）
- **按 ID 取法条**：`getLawById(id)` — 服务法条互链与 AI 展示

检索结果统一结构（示意）：

```javascript
{ type: 'law'|'case'|'faq'|'topic'|'glossary', id, title, subtitle }
```

### 5.2 `law-link.js` — 法条识别算法

#### 5.2.1 阿拉伯数字条号

- 正则：`/第(\d{4})条/g`
- 合法范围：**1119–1163**（继承编）
- `parseLawRefs`：将字符串拆成 `{ text, isLink, lawId }` 片段，供 WXML `wx:for` 渲染可点击 span

#### 5.2.2 中文数字映射

- 表 `CN_NUM_MAP`：`一千一百一十九` → `1119` … 至 `一千一百六十三` → `1163`
- `extractLawIds`：同时扫描阿拉伯与中文表述，去重返回 ID 列表

#### 5.2.3 引用串解析

- `extractLawIdFromCitation(str)`：`/第\s*(\d{1,4})\s*条/` 宽松提取，用于非标准空格场景

```text
输入正文 ──► extractLawIds ──► ['1127','1130']
          ──► parseLawRefs ──► [ {text:'...', isLink:false}, {text:'第1127条 关键词', isLink:true, lawId:'1127'}, ... ]
```

### 5.3 `analytics.js` — 埋点架构

| 能力 | 实现 |
|------|------|
| 存储键 | `yct_analytics` |
| 计数器 | `store.counts[event]++` |
| 环形日志 | `store.log` 最多保留 **300** 条，超出截断尾部 |
| 平台上报 | `wx.reportEvent(event, params)`（基础库支持时） |
| 便捷 API | `logScreen` / `logSearch` / `logContentView` / `logAI` / `logFavorite` |
| 导出 | `exportReport()` — 合并设备信息 + 计数 + 近 50 条事件，便于内测排障 |

### 5.4 `favorites.js` — 收藏 / 笔记 / 历史

| 键 | Storage Key | 结构要点 |
|----|-------------|----------|
| 收藏 | `yct_favorites` | `{ type, id, title, time }[]`，去重 `(type,id)` |
| 笔记 | `yct_notes` | 同键覆盖或新增，`content` 为正文 |
| 历史 | `yct_history` | 同内容置顶，**最多 100 条** |

---

## 6. AI 模块设计

### 6.1 调用链

```
用户输入 ──► wx.request POST DeepSeek chat/completions
                header: Authorization: Bearer <token>
                body: { model: 'deepseek-chat', messages: [ system, ...history ] }
      │
      ▼
助手回复 ──► extractLawIds / parseLawRefs
      │
      ▼
关联案例 findCasesByArticles(legalBasis 命中)
```

### 6.2 System Prompt（摘要）

> 固定为继承法角色；要求回答中引用法条使用「第XXXX条」格式；非法条领域则简短回答并引导回继承法。

（完整字符串见 `subpkg/ai/ai.js` 中 `SYSTEM_PROMPT`。）

### 6.3 会话持久化

- Key：`yct_ai_chat`
- 序列化：`role` + `content`，助理消息在展示层再 enriched `parts` / `relatedCases`
- 上限：**50** 条消息级记录（实现中通过 `while (flat.length > MAX_STORED) flat.shift()`）

### 6.4 安全注意事项（强制）

| 风险 | 要求 |
|------|------|
| API Key 泄露 | **禁止**在生产包体硬编码真实 Key；当前代码若含占位 Key，上线前必须改为**自有后端代理**或**云函数**，由服务端保管密钥 |
| 用户隐私 | Prompt 与弹窗提示勿输入真实身份信息 |
| 域名 | 小程序后台配置 `request` 合法域名（DeepSeek 或代理域名） |
| 内容合规 | 输出为 AI 生成，需免责声明；不提供正式法律意见 |

示例（后端代理思路，非现网代码）：

```javascript
// 小程序仅调用自有域名
wx.request({
  url: 'https://api.your-company.com/v1/ai/inheritance-chat',
  method: 'POST',
  data: { messages: historyForApi }
})
```

---

## 7. 性能优化

| 手段 | 位置 | 说明 |
|------|------|------|
| 分包 + 预下载 | `app.json` | 大案例出主包；`preloadRule` 预热子包 |
| `lazyCodeLoading` | `app.json` | 按需注入，缩短启动 JS 执行 |
| 案例懒加载 | `app.loadCases` | 减少冷启动内存与 IO |
| 搜索防抖 | `pages/search/search.js` | 降低输入时 `search` 调用频率 |
| 图片懒加载 | 列表页 | `image` 组件 `lazy-load`（若使用大图） |
| 日志截断 | `analytics.js` | 防止 Storage 膨胀 |

---

## 8. 广告预留 — AdManager 架构（设计稿）

> 当前仓库可无实现；以下为可插拔扩展，便于与 **RemoteConfig**（远程配置）联动。

### 8.1 模块职责

```
┌─────────────────────────────────────────┐
│              AdManager (singleton)         │
│  - loadRemoteConfig()                    │
│  - shouldShowBanner/placementId()        │
│  - onAdLoad / onAdError / onAdClose      │
└─────────────────────────────────────────┘
          │                    │
          ▼                    ▼
   微信 ad 组件           配置后台 / 云开发 DB
   (banner/custom)       开关、频率、实验分组
```

### 8.2 RemoteConfig 控制逻辑（伪代码）

```javascript
// 设计示意
const AdManager = {
  config: { bannerEnabled: false, intervalSec: 0, placementId: '' },

  async refresh() {
    const remote = await fetchRemoteJSON() // 需合法域名与缓存策略
    this.config = { ...this.config, ...remote.ads }
  },

  shouldShowBanner(page) {
    if (!this.config.bannerEnabled) return false
    if (page === 'mine' && this.config.hideOnMine) return false
    return true
  }
}
```

| 配置项 | 用途 |
|--------|------|
| `bannerEnabled` | 总开关，审核期可一键关闭 |
| `placementId` | 广告位 ID，多环境分离 |
| `frequencyCap` | 每用户每日展示上限 |
| `whitelistPages` | 仅指定页展示，降低体验干扰 |

---

## 9. 埋点方案 — 事件列表

### 9.1 自动/半自动事件（与代码对齐）

| 事件名 | 触发函数 | 主要参数 | 说明 |
|--------|----------|----------|------|
| `app_launch` | `analytics.log` | — | 小程序启动 |
| `screen_view` | `logScreen` | `screen` | 各页 `onShow` |
| `search` | `logSearch` | `query`, `results` | 全局搜索执行 |
| `content_view` | `logContentView` | `type`, `id` | 法条/案例/工具等内容浏览 |
| `ai_query` | `logAI` | `query`（截断） | 用户发起 AI 提问 |
| `favorite` | `logFavorite` | `type`, `id` | 收藏行为（在收藏操作时调用） |

### 9.2 页面与 `screen` 建议取值

| 页面 | 建议 `screen` 值 |
|------|------------------|
| 首页 | `home` |
| 法律库 | `laws` |
| 案例库 | `cases` |
| 工具箱 | `tools` |
| 我的 | `mine` |
| 搜索 | `search` |
| 法条详情 | `law_detail` |
| 案例详情 | `case_detail` |
| 工具详情 | `tool_detail` |
| 词典 | `glossary` |
| AI | `ai_assistant` |
| 收藏/笔记/历史 | `favorites` / `notes` / `history` |
| 联系 | `contact` |

### 9.3 数据流

```
页面/工具函数
      │ log(...)
      ▼
analytics.js ──► wx.setStorageSync('yct_analytics')
      │
      └──► wx.reportEvent (若可用) ──► 微信数据分析/实验平台
```

---

## 10. 安全与合规

| 领域 | 措施 |
|------|------|
| 隐私政策 | `mine` 页弹窗说明本地存储范围与 AI 使用注意 |
| 本地数据 | 收藏/笔记/历史/埋点/AI 缓存仅存用户设备；卸载小程序即清除（除非微信云备份） |
| 传输安全 | AI 请求使用 HTTPS；证书校验由微信客户端处理 |
| API 安全 | Key **服务端**保管；小程序只持有短期 token 或无密钥 |
| 内容免责 | 法条与案例仅供学习参考；AI 输出非法律意见 |
| `urlCheck` | 开发期可能关闭域名校验；**提审前**必须开启合法域名 |

---

## 附录 A：核心检索伪代码

```javascript
search(query) {
  if (!query || query.length < 2) return []
  const q = query.toLowerCase()
  const results = []
  // laws: title, plainExplanation, originalText, keywords
  // cases: 需 casesLoaded
  // faq, topics, glossary: 各自字段 includes
  return results
}
```

---

## 附录 B：法条跳转 URL 约定

```
/subpkg/law-detail/law-detail?id=1127
```

---

*本文档与实现不一致时，以代码与微信审核要求为准；重大变更需同步更新 PRD 与本 TDD。*
