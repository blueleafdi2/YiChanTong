# 遗产通 — 中国遗产继承法律百科 App

## 📱 产品简介

**遗产通**是一款面向全体中国公民的遗产继承法律百科 App，目标是让每个人都能"看得懂、查得到、用得上"继承相关法律知识。

### 核心特色

- **全面覆盖**：《民法典》继承编全部 45 条法条 + 司法解释 + 历史法规
- **三层内容**：一句话结论 → 生活案例 → 法条原文，满足小白和专业人士
- **65+ 真实案例**：来自全国各级法院的继承纠纷案例，含案号、法院、裁判要旨
- **20 大知识专题**：房产继承、遗嘱效力、数字遗产等专题深度解析
- **30 个常见问答**：覆盖高频继承法律问题
- **6 大实用工具**：继承计算器、遗嘱模板、流程指引等
- **54 个法律术语**：白话解释，零基础也能看懂
- **纯离线可用**：无需网络，随时随地查询

---

## 📥 安装 APK（小白指南）

### 方法一：直接安装（推荐）

1. 将 `遗产通-v4.0.0-debug.apk` 文件传输到安卓手机
   - 可通过 USB 数据线、微信/QQ 发送、或邮件附件
2. 在手机上点击该 APK 文件
3. 如果提示"未知来源"，按提示允许安装
   - 设置 → 安全 → 允许安装未知来源应用
4. 安装完成后即可使用

### 方法二：使用 ADB 安装

```bash
# 连接手机（需开启开发者模式和 USB 调试）
adb install 遗产通-v4.0.0-debug.apk
```

> **注意**：debug 版本可直接安装。release 版本（`遗产通-v4.0.0-release.apk`）未签名，如需安装需要先签名。

---

## 🛠️ 开发者指南

### 环境要求

| 工具 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Android SDK | API 36 (build-tools 36.1.0) |
| Gradle | 8.11.1（项目内含 wrapper，无需单独安装） |

### 快速开始

```bash
# 1. 进入项目目录
cd JiChengTong

# 2. 设置 Android SDK 路径（根据实际路径修改）
export ANDROID_HOME=~/Library/Android/sdk
export ANDROID_SDK_ROOT=~/Library/Android/sdk

# 3. 构建 Debug APK
./gradlew assembleDebug

# 4. APK 输出位置
# app/build/outputs/apk/debug/app-debug.apk

# 5. 构建 Release APK
./gradlew assembleRelease
# app/build/outputs/apk/release/app-release-unsigned.apk
```

### 项目结构

```
JiChengTong/
├── app/
│   ├── build.gradle                          # App 级构建配置
│   ├── src/main/
│   │   ├── AndroidManifest.xml               # 应用清单
│   │   ├── java/com/jichengtong/app/
│   │   │   ├── activities/                   # Activity 类
│   │   │   │   ├── MainActivity.java         # 主界面（底部导航）
│   │   │   │   ├── LawDetailActivity.java    # 法条详情
│   │   │   │   ├── CaseDetailActivity.java   # 案例详情
│   │   │   │   ├── TopicDetailActivity.java  # 专题/FAQ 详情
│   │   │   │   ├── SearchActivity.java       # 搜索
│   │   │   │   ├── ContactActivity.java      # 联系专家
│   │   │   │   └── ToolsDetailActivity.java  # 工具详情
│   │   │   ├── fragments/                    # Fragment 类
│   │   │   │   ├── HomeFragment.java         # 首页
│   │   │   │   ├── LawsFragment.java         # 法律库
│   │   │   │   ├── CasesFragment.java        # 案例库
│   │   │   │   ├── ToolsFragment.java        # 工具箱
│   │   │   │   └── MineFragment.java         # 我的
│   │   │   ├── adapters/                     # RecyclerView 适配器
│   │   │   ├── models/                       # 数据模型
│   │   │   ├── data/                         # 数据加载层
│   │   │   │   └── DataProvider.java         # 单例数据提供者
│   │   │   └── utils/                        # 工具类
│   │   ├── assets/                           # 数据文件
│   │   │   ├── laws/
│   │   │   │   └── civil_code_inheritance.json  # 民法典继承编 45 条全文
│   │   │   ├── cases/
│   │   │   │   └── court_cases.json             # 65+ 法院案例
│   │   │   ├── knowledge/
│   │   │   │   ├── topics.json                  # 20 大知识专题
│   │   │   │   ├── faq.json                     # 30 个常见问答
│   │   │   │   └── glossary.json                # 54 个法律术语
│   │   │   └── tools/
│   │   │       └── tools_data.json              # 6 大工具数据
│   │   └── res/                              # 资源文件
│   │       ├── layout/                       # 布局 XML
│   │       ├── drawable/                     # 矢量图标
│   │       ├── values/                       # 颜色、字符串、主题
│   │       └── menu/                         # 底部导航菜单
├── build.gradle                              # 项目级构建配置
├── settings.gradle
├── gradle.properties
├── gradlew                                   # Gradle wrapper 脚本
└── gradle/wrapper/
    ├── gradle-wrapper.jar
    └── gradle-wrapper.properties
```

### 技术栈

- **语言**：Java 17
- **UI 框架**：Android Material Design 3 (Material You)
- **构建工具**：Gradle 8.11.1 + Android Gradle Plugin 8.7.3
- **数据格式**：JSON（Gson 解析）
- **最低 SDK**：Android 7.0 (API 24)
- **目标 SDK**：Android 16 (API 36)

### 如何添加更多案例

编辑 `app/src/main/assets/cases/court_cases.json`，按照以下格式添加：

```json
{
  "id": "case_066",
  "caseNumber": "(2024)沪01民终5678号",
  "title": "王某诉赵某遗嘱继承纠纷案",
  "court": "上海市第一中级人民法院",
  "courtLevel": "中级人民法院",
  "province": "上海",
  "city": "上海",
  "judgeDate": "2024-06",
  "caseType": "遗嘱继承纠纷",
  "tags": ["自书遗嘱", "遗嘱效力", "见证人"],
  "caseSummary": "案情描述...",
  "disputeFocus": "争议焦点...",
  "judgment": "裁判结果...",
  "legalBasis": ["《民法典》第1134条", "《民法典》第1135条"],
  "rulingGist": "裁判要旨...",
  "source": "中国裁判文书网"
}
```

### 如何签名 Release APK

```bash
# 1. 生成签名密钥（首次）
keytool -genkey -v -keystore jichengtong.jks -alias jichengtong \
  -keyalg RSA -keysize 2048 -validity 10000

# 2. 签名 APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore jichengtong.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk jichengtong

# 3. 优化 APK
zipalign -v 4 app-release-unsigned.apk 遗产通-v4.0.0-release-signed.apk
```

---

## 📊 内容覆盖一览

| 内容类型 | 数量 | 说明 |
|---------|------|------|
| 法律条文 | 45 条 | 《民法典》继承编第1119-1163条全文 |
| 白话解读 | 45 篇 | 每条法律对应一篇通俗解读 |
| 生活案例 | 45 个 | 每条法律对应一个生活化场景 |
| 司法解释 | 45 条 | 每条法律关联的司法解释 |
| 立法沿革 | 45 条 | 1985年继承法到民法典的演变 |
| 法院案例 | 65+ 个 | 覆盖8大案由类型、全国多省份法院 |
| 知识专题 | 20 个 | 从房产继承到数字遗产 |
| 常见问答 | 30 个 | 覆盖高频法律问题 |
| 实用工具 | 6 个 | 计算器、模板、流程指引 |
| 法律术语 | 54 个 | 白话解释法律专业名词 |

---

## ⚠️ 免责声明

本 App 内容基于中华人民共和国现行有效法律法规编写，仅供学习参考，**不构成专业法律意见**。具体法律问题请咨询执业律师。

数据来源：
- 全国人民代表大会官网
- 最高人民法院官网
- 中国裁判文书网
- 中国司法案例网

内容更新至 **2024 年 12 月**。

---

## 📞 联系方式

- 全国法律服务热线：**12348**（免费）
- 中国法律服务网：www.12348.gov.cn
- 邮箱：legal@jichengtong.com
