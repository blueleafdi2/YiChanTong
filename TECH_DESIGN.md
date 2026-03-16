# 遗产通 (YiChanTong) — Technical Design Document

## 1. Architecture Overview

### Technology Stack
- **Platform**: Android native (Java)
- **Language**: Java 17
- **Build**: Gradle 8.11.1, Android Gradle Plugin 8.7.3
- **SDK**: compileSdk 36, minSdk 24, targetSdk 36
- **UI**: Material Design 3 (Material Components 1.12.0)

### Design Principles
- **Offline-first**: All core content (laws, cases, glossary, tools, topics, FAQs) stored as JSON in `assets/`, no network required for browsing
- **SharedPreferences**: User data (favorites, notes, history, AI chat) persisted via SharedPreferences
- **Single Activity + Fragments**: MainActivity hosts bottom navigation with 5 fragments

### Key Dependencies
```groovy
implementation 'com.google.android.material:material:1.12.0'
implementation 'androidx.appcompat:appcompat:1.7.0'
implementation 'androidx.recyclerview:recyclerview:1.4.0'
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'androidx.viewpager2:viewpager2:1.1.0'
implementation 'com.google.code.gson:gson:2.11.0'
```

---

## 2. Module Structure

```
com.jichengtong.app
├── activities/
│   ├── MainActivity.java          # Host activity, bottom nav
│   ├── LawDetailActivity.java     # Law article detail
│   ├── CaseDetailActivity.java    # Case detail
│   ├── TopicDetailActivity.java   # FAQ / Topic detail
│   ├── SearchActivity.java        # Full-text search
│   ├── AIActivity.java            # AI chat (DeepSeek)
│   ├── GlossaryActivity.java      # Legal glossary
│   ├── ToolsDetailActivity.java   # Tool content + Word export
│   ├── WebViewActivity.java       # Official source / HTML viewer
│   ├── FavoritesActivity.java
│   ├── NotesActivity.java
│   ├── HistoryActivity.java
│   └── ContactActivity.java
├── adapters/
│   ├── LawAdapter.java
│   ├── CaseAdapter.java
│   ├── ToolAdapter.java
│   ├── TopicAdapter.java
│   ├── QuestionAdapter.java
│   └── (SearchResultAdapter, ChatAdapter, GlossaryAdapter - inner classes)
├── data/
│   └── DataProvider.java          # Singleton, lazy-loads JSON from assets
├── fragments/
│   ├── HomeFragment.java
│   ├── LawsFragment.java
│   ├── CasesFragment.java
│   ├── ToolsFragment.java
│   └── MineFragment.java
├── models/
│   ├── LawArticle.java
│   ├── CourtCase.java
│   ├── GlossaryItem.java
│   ├── Topic.java
│   ├── FAQ.java
│   └── ToolItem.java
└── utils/
    ├── FavoritesManager.java      # SharedPreferences for favorites, notes, history
    ├── LawLinkHelper.java         # Clickable law references → LawDetailActivity
    └── GlossaryHelper.java        # Term popup, glossary highlighting
```

### Asset Structure
```
assets/
├── laws/
│   └── civil_code_inheritance.json   # 45 articles (1119-1163)
├── cases/
│   └── court_cases.json              # 390 cases
├── knowledge/
│   ├── glossary.json                 # 125 terms
│   ├── topics.json                   # 20 topics
│   └── faq.json                      # 30 FAQs
├── tools/
│   └── tools_data.json               # 6 tools
└── images/
    └── wechat_qr.png
```

---

## 3. Data Flow

### DataProvider
- **Singleton**: `DataProvider.getInstance(context)`
- **Lazy loading**: Each content type loaded on first access, cached in memory
- **Methods**: `getLawArticles()`, `getCourtCases()`, `getGlossary()`, `getTopics()`, `getFAQs()`, `getTools()`
- **Search**: `search(query)` — unified search across all content types

### Search Flow
1. User types in SearchActivity (min 2 chars)
2. `DataProvider.search(query)` returns `List<Object>`
3. SearchResultAdapter renders with type badges (法条/案例/问答/专题/工具/术语)
4. Empty state shows AI redirect with optional preset question

### Navigation Helpers
- **LawLinkHelper**: Converts `第XXXX条` / `《民法典》第XXXX条` / Chinese numerals to clickable spans → LawDetailActivity
- **GlossaryHelper**: `highlightGlossaryTerms()` for hard terms; `showTermDialog()` for popup

### FavoritesManager
- SharedPreferences key: `jichengtong_prefs`
- Keys: `favorites`, `notes`, `history`
- Structure: `List<Map<String,String>>` with type, id, title, time/note

---

## 4. Key Technical Decisions

### AI Streaming
- **Problem**: RecyclerView rebind on every chunk causes jank
- **Solution**: Throttle UI updates to 120ms; during streaming, update via `findViewHolderForAdapterPosition` + direct `TextView.setText` instead of `notifyItemChanged`
- **Two-phase**: Stream text first; after `[DONE]`, parse `【第XXXX条】` and `【案例:关键词】`, fetch related content, append RelatedItem chips

### Case Tags (ChipGroup)
- ChipGroup with `app:singleSelection="false"` for type filter
- Tags support wrapping; case tags with glossary terms show `ic_help` icon, click opens GlossaryHelper dialog

### Word Export
- Generate HTML with `xmlns:o`, `xmlns:w` for Word compatibility
- **Chinese font stack**: `'Microsoft YaHei','PingFang SC','Hiragino Sans GB','WenQuanYi Micro Hei','Noto Sans CJK SC',SimSun,STSong,'宋体'`
- Save via `Intent.ACTION_CREATE_DOCUMENT` with `application/msword`

### WebView for Official Sources
- **Viewport**: `<meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no'>`
- **Mobile-friendly CSS**: Injected via `loadUrl("javascript:...")` to increase font size, line height
- **Case source page**: Two buttons — "前往官网" and "搜索本案裁判文书" (wenshu.court.gov.cn docId URL)

### Glossary Highlighting
- **Scope**: Only `difficulty=hard` terms
- **Occurrence**: First occurrence only (`indexOf`)
- **Style**: Purple color (0xFF6A1B9A), no underline in law detail; in GlossaryHelper `highlightGlossaryTerms` uses `ds.setUnderlineText(false)`

---

## 5. Build & Signing

### Debug
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release
```bash
./gradlew assembleRelease
# Then:
zipalign -v 4 app-release-unsigned.apk app-release-aligned.apk
apksigner sign --ks yichangtong-release.jks --out app-release.apk app-release-aligned.apk
```

### Keystore
- Release keystore: `yichangtong-release.jks`
- Store credentials securely (e.g., in local.properties or CI secrets)

---

## 6. API Integrations

### DeepSeek API
- **Endpoint**: `https://api.deepseek.com/chat/completions`
- **Model**: `deepseek-chat`
- **Streaming**: `stream: true`, `Accept: text/event-stream`
- **Auth**: `Authorization: Bearer <API_KEY>`
- **Request**: JSON with `messages` array (system + user/assistant history, max ~21 messages)
- **Response**: SSE chunks, parse `data: {...}` until `[DONE]`

---

## 7. Performance Optimizations

| Area | Optimization |
|------|--------------|
| Glossary highlighting | Only hard terms, first occurrence; avoid full-text scan on every bind |
| AI streaming | 120ms throttle; direct TextView update during stream; avoid RecyclerView rebind |
| Large JSON | Lazy load via DataProvider singleton; load once per content type |
| WebView | `LOAD_DEFAULT` cache; `setTextZoom(120)` for readability |
| Search | Debounce implicit via `afterTextChanged`; no debounce timer (instant search) |

---

## 8. AndroidManifest Summary

| Activity | Exported | Parent |
|----------|----------|--------|
| MainActivity | true (launcher) | — |
| LawDetailActivity | false | MainActivity |
| CaseDetailActivity | false | MainActivity |
| TopicDetailActivity | false | MainActivity |
| SearchActivity | false | MainActivity |
| AIActivity | false | MainActivity |
| GlossaryActivity | false | MainActivity |
| ToolsDetailActivity | false | MainActivity |
| WebViewActivity | false | MainActivity |
| FavoritesActivity | false | MainActivity |
| NotesActivity | false | MainActivity |
| HistoryActivity | false | MainActivity |
| ContactActivity | false | MainActivity |

**Permissions**: `INTERNET`, `CALL_PHONE`
