# 遗产通 App 广告变现完整指南

> **目标读者**：王迪（个人开发者，零广告经验）  
> **应用**：遗产通 (YiChanTong) — 中国遗产继承法律百科  
> **文档版本**：1.0 | 更新日期：2026年3月

---

## 目录

1. [广告变现时间表](#1-广告变现时间表)
2. [中国市场广告联盟对比表](#2-中国市场广告联盟对比表)
3. [穿山甲接入详细步骤](#3-穿山甲接入详细步骤)
4. [优量汇接入步骤](#4-优量汇接入步骤)
5. [广告位规划](#5-广告位规划)
6. [RemoteConfig 广告控制方案](#6-remoteconfig-广告控制方案)
7. [收入预估](#7-收入预估)
8. [注意事项](#8-注意事项)
9. [行动清单](#9-行动清单)

---

## 1. 广告变现时间表

### Phase 0：现在 / 上架前（日活 0–100）

**目标**：只注册账号，**绝不**集成 SDK。

| 序号 | 行动项 | 说明 |
|------|--------|------|
| 1 | 穿山甲注册 | 访问 https://www.csjplatform.com 注册开发者账号 |
| 2 | 优量汇注册 | 访问 https://adnet.qq.com/register 注册腾讯广告联盟 |
| 3 | 快手联盟注册 | 访问 https://u.kuaishou.com 注册（可选） |
| 4 | 百度联盟注册 | 访问 https://union.baidu.com 注册（可选） |
| 5 | 准备资质 | 软著、营业执照（如有）、应用商店上架证明 |

**重要**：
- 不添加任何广告 SDK 依赖
- 不修改 `remote_config.json` 中的 `ad_config` 开关（保持 `banner_enabled: false`）
- 上架审核时应用内无广告，更容易通过

**若穿山甲要求企业资质**：可优先完成优量汇（腾讯）注册，其对个人开发者更友好。

---

### Phase 1：日活 100–500

**目标**：完善广告账号，创建广告位，准备接入。

| 序号 | 行动项 | 说明 |
|------|--------|------|
| 1 | 完成穿山甲资质认证 | 上传软著、应用商店截图、对公/个人账户 |
| 2 | 创建应用 | 在穿山甲后台创建「遗产通」应用，提交审核 |
| 3 | 创建广告位 | 创建 Banner、插屏、激励视频、原生广告位 |
| 4 | 获取测试广告位 ID | 记录各广告形式的测试 ID，用于开发阶段 |
| 5 | 优量汇同理 | 完成认证、创建媒体、创建广告位 |

---

### Phase 2：日活 1000+

**目标**：通过 RemoteConfig 接入第一个广告 SDK。

| 序号 | 行动项 | 说明 |
|------|--------|------|
| 1 | 集成穿山甲 SDK | 添加 Gradle 依赖，初始化，加载广告 |
| 2 | 使用 RemoteConfig 控制 | `ad_config.banner_enabled = true` 时显示广告 |
| 3 | 先开 Banner | 首页底部 Banner，对体验影响最小 |
| 4 | 灰度发布 | 通过 RemoteConfig 只对部分用户开启 |
| 5 | 观察数据 | 留存、崩溃率、用户反馈 |

---

### Phase 3：日活 5000+

**目标**：多网络优化，提升填充率与 eCPM。

| 序号 | 行动项 | 说明 |
|------|--------|------|
| 1 | 接入优量汇 | 作为备选网络，穿山甲填充不足时补充 |
| 2 | 考虑聚合平台 | GroMore（穿山甲旗下）、穿山甲聚合等 |
| 3 | A/B 测试 | 不同广告位、频次、形式的收益对比 |
| 4 | 优化广告位 | 根据数据调整位置与形式 |

---

## 2. 中国市场广告联盟对比表

| 平台名 | 注册门槛 | 最低日活要求 | 广告形式 | eCPM 参考（¥/千次） | 结算方式 | 适合阶段 |
|--------|----------|--------------|----------|----------------------|----------|----------|
| **穿山甲**（字节跳动/巨量引擎） | 企业为主，部分个人可申请；需软著+应用上架 | 无硬性要求，建议 500+ | Banner、插屏、激励视频、原生、开屏 | Banner 5–15<br>插屏 30–80<br>激励 60–150<br>原生 20–50 | 月结，满 100 元提现 | Phase 2 首选 |
| **优量汇**（腾讯广告） | 企业/个人均可，需实名认证 | 无硬性要求 | Banner、插屏、激励视频、原生、开屏 | 略低于穿山甲，约 80% | 月结，满 100 元提现 | Phase 2 备选 / Phase 3 |
| **快手联盟** | 需应用上架，实名认证 | 建议 1000+ | 激励视频、插屏、Banner、原生 | 激励视频较高 | 月结 | Phase 3 |
| **百度联盟** | 需网站/应用，实名认证 | 无硬性要求 | Banner、插屏、激励、信息流 | 中等 | 月结 | Phase 3 备选 |
| **华为广告联盟** (HUAWEI Ads) | 华为开发者账号，应用上架华为应用市场 | 无硬性要求 | Banner、插屏、激励、原生 | 华为设备用户 eCPM 较高 | 月结 | 华为渠道用户多时 |
| **AdMob**（Google） | Google 账号，需 18+ | 无硬性要求 | 全形式 | 海外用户高，国内需 VPN 可能受限 | 月结，$100 起 | 海外用户时使用 |

**推荐策略**：  
- 国内主推：**穿山甲**（填充好、eCPM 高、文档全）  
- 备选：**优量汇**（腾讯系，用户覆盖广）  
- 海外：**AdMob**（若有海外用户）

---

## 3. 穿山甲接入详细步骤

### 3.1 账号注册

1. 访问：https://www.csjplatform.com  
2. 点击「注册」，使用邮箱注册  
3. 登录后进入「接入中心」→「广告变现」

### 3.2 应用创建与审核

1. 进入「流量管理」→「应用管理」→「添加应用」  
2. 填写应用信息：
   - 应用名称：遗产通  
   - 应用包名：`com.jichengtong.app`  
   - 应用分类：工具 / 教育  
   - 下载链接：应用商店链接或 APK 下载地址  
3. 上传软著扫描件、应用商店截图  
4. 提交审核，通常 1–3 个工作日

### 3.3 广告位创建

在应用审核通过后，为每种广告形式创建广告位：

| 广告形式 | 建议命名 | 用途 |
|----------|----------|------|
| Banner | 遗产通_首页底部_Banner | 首页底部 |
| 插屏 | 遗产通_AI对话后_插屏 | AI 对话结束后 |
| 激励视频 | 遗产通_解锁内容_激励 | 解锁高级内容 |
| 原生 | 遗产通_案例详情_原生 | 案例详情页信息流 |

### 3.4 SDK 集成

#### Step 1：添加 Maven 仓库与 Gradle 依赖

在 `JiChengTong/settings.gradle` 或项目级 `build.gradle` 中添加穿山甲 Maven 仓库：

```gradle
// settings.gradle 的 dependencyResolutionManagement.repositories 中添加：
maven { url 'https://artifact.bytedance.com/repository/pangle' }
```

在 `JiChengTong/app/build.gradle` 的 `dependencies` 中添加：

```gradle
// 穿山甲 SDK（以穿山甲后台「接入中心」最新版本为准）
implementation 'com.pangle.cn:ads-sdk:4.4.0.9'  // 示例版本，请查阅官网
```

> **提示**：穿山甲 SDK 需从 `artifact.bytedance.com` 拉取，请确保网络可访问。最新版本号请登录穿山甲后台 → 接入中心 → 广告变现 → 下载 SDK 查看。

#### Step 2：初始化（Application 或 MainActivity）

```java
// 在 Application 或 MainActivity.onCreate 中
// 包名以穿山甲官方文档为准，可能为 com.bytedance.sdk.openadsdk 或 com.pangle
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdSdk;

// 初始化穿山甲（仅在 ad_config.provider == "pangle" 时执行）
public void initPangle(Context context) {
    TTAdSdk.init(context, new TTAdConfig.Builder()
        .appId("你的穿山甲 App ID")  // 从穿山甲后台「应用管理」获取
        .useTextureView(true)
        .appName("遗产通")
        .build());
}
```

> **注意**：具体初始化 API 以穿山甲官方 Android 集成文档为准。

#### Step 3：Banner 广告加载示例

```java
// 在 HomeFragment 或需要展示 Banner 的页面
private void loadBannerAd(ViewGroup adContainer) {
    RemoteConfig rc = RemoteConfig.getInstance(requireContext());
    if (!rc.isBannerAdEnabled() || TextUtils.isEmpty(rc.getAdUnitId())) {
        adContainer.setVisibility(View.GONE);
        return;
    }
    
    String adUnitId = rc.getAdUnitId();  // 从 RemoteConfig 获取，可远程切换
    TTAdNative adNative = TTAdSdk.getAdManager().createAdNative(requireContext());
    
    AdSlot adSlot = new AdSlot.Builder()
        .setCodeId(adUnitId)
        .setSupportDeepLink(true)
        .setAdCount(1)
        .setExpressViewAcceptedSize(360, 0)  // Banner 宽度 360dp
        .build();
    
    adNative.loadBannerAd(adSlot, new TTAdNative.BannerAdListener() {
        @Override
        public void onError(int code, String message) {
            adContainer.setVisibility(View.GONE);
        }
        @Override
        public void onBannerAdLoad(View view) {
            adContainer.removeAllViews();
            adContainer.addView(view);
            adContainer.setVisibility(View.VISIBLE);
        }
    });
}
```

#### Step 4：插屏广告加载示例（AI 对话结束后）

```java
// 在 AIActivity 中，用户发送消息并收到 AI 回复后
private void maybeShowInterstitial() {
    RemoteConfig rc = RemoteConfig.getInstance(this);
    if (!rc.isInterstitialAdEnabled()) return;
    
    TTAdNative adNative = TTAdSdk.getAdManager().createAdNative(this);
    AdSlot slot = new AdSlot.Builder()
        .setCodeId(rc.getInterstitialAdUnitId())  // 需在 RemoteConfig 中扩展
        .setSupportDeepLink(true)
        .build();
    
    adNative.loadInterstitialAd(slot, new TTAdNative.InterstitialAdListener() {
        @Override
        public void onError(int code, String message) { }
        @Override
        public void onInterstitialAdLoad(TTInterstitialAd ad) {
            ad.showInterstitialAd(AIActivity.this);
        }
    });
}
```

### 3.5 使用 RemoteConfig 控制广告展示

- 所有广告加载前必须检查 `RemoteConfig.isBannerAdEnabled()` / `isInterstitialAdEnabled()`  
- 广告位 ID 从 `RemoteConfig.getAdUnitId()` 等接口获取，便于远程切换  
- 上线前将 `remote_config.json` 中 `banner_enabled`、`interstitial_enabled` 设为 `false`，审核通过后再改为 `true`

### 3.6 测试广告位 ID

穿山甲提供测试广告位 ID，开发阶段使用，避免点击自己的广告违规：

| 广告形式 | 测试广告位 ID（示例，以官方文档为准） |
|----------|--------------------------------------|
| Banner | 通常格式如 `901121234`，请在穿山甲后台「开发测试」中查看 |
| 插屏 | 同上 |
| 激励视频 | 同上 |
| 原生 | 同上 |

**重要**：正式上线前必须替换为真实广告位 ID。

---

## 4. 优量汇接入步骤

### 4.1 账号注册

1. 访问：https://adnet.qq.com/register  
2. 使用 QQ 或微信登录  
3. 填写开发者信息（企业名称需与营业执照一致，个人开发者填写个人姓名）  
4. 提交资质，等待审核（约 1 个工作日）

### 4.2 创建媒体与应用

1. 登录后进入「流量合作」→「媒体管理」  
2. 添加媒体（应用）  
3. 填写应用名称「遗产通」、包名 `com.jichengtong.app`  
4. 获取 **媒体 ID（AppId）**

### 4.3 创建广告位

1. 在应用下创建广告位  
2. 选择广告形式：Banner / 插屏 / 激励视频 / 原生  
3. 获取 **广告位 ID（PosId）**

### 4.4 SDK 集成

#### Gradle 依赖

```gradle
// 优量汇 SDK（GDT）
implementation 'com.qq.e.union:union:4.600.1340'  // 以官方最新版本为准
```

#### 初始化

```java
// 在 Application 中
import com.qq.e.comm.managers.GDTAdSdk;

GDTAdSdk.init(context, "你的媒体ID");
```

#### Banner 加载示例

```java
// 与穿山甲类似，通过 RemoteConfig 判断 provider
if ("gdt".equals(rc.getAdProvider())) {
    // 使用优量汇 Banner
}
```

### 4.5 注意事项

- 新创建的广告位约 30 分钟后才能拉取到广告  
- 需完成企业/个人资质认证才能正常变现  
- 隐私政策中需说明 SDK 收集的数据及用途  

---

## 5. 广告位规划

针对遗产通 App 的页面结构，推荐以下广告位布局：

### 5.1 首页底部 Banner

| 项目 | 说明 |
|------|------|
| 位置 | `fragment_home.xml` 底部，`NestedScrollView` 内、免责声明上方 |
| 布局 | 在「知识专题」RecyclerView 与 Disclaimer 之间插入 `FrameLayout` 作为广告容器 |
| 尺寸 | 320×50 或 自适应宽度 |
| 用户体验 | 不遮挡核心内容，可随页面滚动 |

**布局示意**：

```
┌─────────────────────────────┐
│ 遗产通 Header + 搜索栏       │
├─────────────────────────────┤
│ 快速了解 - 场景 Chips        │
├─────────────────────────────┤
│ AI 法律助手 入口卡片         │
├─────────────────────────────┤
│ 典型案例推荐                 │
├─────────────────────────────┤
│ 热门问题                     │
├─────────────────────────────┤
│ 知识专题                     │
├─────────────────────────────┤
│ [Banner 广告位]  ← 新增      │
├─────────────────────────────┤
│ 免责声明                     │
└─────────────────────────────┘
```

### 5.2 案例详情页原生广告

| 项目 | 说明 |
|------|------|
| 位置 | `activity_case_detail`，案例摘要与判决内容之间 |
| 形式 | 原生信息流，样式与案例卡片一致 |
| 用户体验 | 融入内容流，标注「广告」标识 |

**布局示意**：

```
┌─────────────────────────────┐
│ 案例标题、案号、法院、日期    │
├─────────────────────────────┤
│ 案例摘要                     │
├─────────────────────────────┤
│ [原生广告 - 样式类似案例卡片] │
├─────────────────────────────┤
│ 法院判决                     │
├─────────────────────────────┤
│ 标签、收藏、分享             │
└─────────────────────────────┘
```

### 5.3 AI 对话结束后插屏

| 项目 | 说明 |
|------|------|
| 时机 | 用户发送问题 → AI 返回完整回复后，延迟 1–2 秒弹出 |
| 频次 | 每 3 次对话最多 1 次插屏（需在 RemoteConfig 中配置） |
| 用户体验 | 不打断输入，在自然停顿点展示 |

### 5.4 法条详情页底部 Banner

| 项目 | 说明 |
|------|------|
| 位置 | `activity_law_detail` 底部，原文与司法解释下方 |
| 布局 | 固定底部或嵌入 ScrollView 底部 |
| 尺寸 | 320×50 |

### 5.5 激励视频解锁高级内容

| 项目 | 说明 |
|------|------|
| 场景 | 未来若有「高级案例解读」「深度分析」等付费/会员内容 |
| 逻辑 | 用户点击「观看广告解锁」→ 播放激励视频 → 解锁内容 |
| 用户体验 | 自愿观看，不强制 |

---

## 6. RemoteConfig 广告控制方案

### 6.1 完整 Schema

在 `remote_config.json` 中扩展 `ad_config` 为以下结构：

```json
{
  "ad_config": {
    "enabled": false,
    "provider": "pangle",
    "placements": {
      "home_banner": {
        "enabled": false,
        "ad_unit_id": "",
        "test_ad_unit_id": ""
      },
      "case_detail_native": {
        "enabled": false,
        "ad_unit_id": "",
        "test_ad_unit_id": ""
      },
      "ai_interstitial": {
        "enabled": false,
        "ad_unit_id": "",
        "test_ad_unit_id": "",
        "min_conversations_between_ads": 3
      },
      "law_detail_banner": {
        "enabled": false,
        "ad_unit_id": "",
        "test_ad_unit_id": ""
      },
      "rewarded_unlock": {
        "enabled": false,
        "ad_unit_id": "",
        "test_ad_unit_id": ""
      }
    },
    "frequency_cap": {
      "interstitial_per_session_max": 2,
      "interstitial_interval_seconds": 120
    },
    "user_segments": {
      "new_user_days": 7,
      "new_user_interstitial_enabled": false,
      "new_user_banner_enabled": true
    }
  }
}
```

### 6.2 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `enabled` | boolean | 全局广告开关，false 时所有广告不展示 |
| `provider` | string | `"pangle"` \| `"gdt"` \| `"admob"`，选择广告网络 |
| `placements.*.enabled` | boolean | 单个广告位开关 |
| `placements.*.ad_unit_id` | string | 正式广告位 ID |
| `placements.*.test_ad_unit_id` | string | 测试广告位 ID（Debug 包使用） |
| `placements.ai_interstitial.min_conversations_between_ads` | int | 插屏间隔：每 N 次对话最多 1 次插屏 |
| `frequency_cap.interstitial_per_session_max` | int | 单次启动最多展示插屏次数 |
| `frequency_cap.interstitial_interval_seconds` | int | 插屏最小间隔（秒） |
| `user_segments.new_user_days` | int | 新用户定义：注册/首次启动 N 天内 |
| `user_segments.new_user_interstitial_enabled` | boolean | 新用户是否展示插屏（建议 false） |
| `user_segments.new_user_banner_enabled` | boolean | 新用户是否展示 Banner |

### 6.3 RemoteConfig.java 扩展方法

在现有 `RemoteConfig` 类中增加：

```java
public boolean isAdEnabled() {
    try {
        return config.optJSONObject("ad_config") != null
            && config.getJSONObject("ad_config").optBoolean("enabled", false);
    } catch (Exception e) { return false; }
}

public String getAdProvider() {
    try {
        return config.getJSONObject("ad_config").optString("provider", "pangle");
    } catch (Exception e) { return "pangle"; }
}

public boolean isPlacementEnabled(String placement) {
    try {
        JSONObject p = config.getJSONObject("ad_config").optJSONObject("placements");
        if (p == null) return false;
        JSONObject pl = p.optJSONObject(placement);
        return pl != null && pl.optBoolean("enabled", false);
    } catch (Exception e) { return false; }
}

public String getPlacementAdUnitId(String placement, boolean useTest) {
    try {
        JSONObject pl = config.getJSONObject("ad_config")
            .getJSONObject("placements").optJSONObject(placement);
        if (pl == null) return "";
        String key = useTest ? "test_ad_unit_id" : "ad_unit_id";
        return pl.optString(key, pl.optString("ad_unit_id", ""));
    } catch (Exception e) { return ""; }
}

public int getInterstitialMinConversations() {
    try {
        return config.getJSONObject("ad_config")
            .getJSONObject("placements").optJSONObject("ai_interstitial")
            .optInt("min_conversations_between_ads", 3);
    } catch (Exception e) { return 3; }
}
```

### 6.4 向后兼容

保留原有 `banner_enabled`、`interstitial_enabled`、`ad_unit_id`，新逻辑优先读取 `placements`，缺失时回退到旧字段。

---

## 7. 收入预估

### 7.1 中国市场 eCPM 参考（¥/千次展示）

| 广告形式 | 保守 | 中等 | 乐观 |
|----------|------|------|------|
| Banner | ¥5 | ¥10 | ¥15 |
| 插屏 | ¥30 | ¥50 | ¥80 |
| 激励视频 | ¥60 | ¥100 | ¥150 |
| 原生 | ¥20 | ¥35 | ¥50 |

### 7.2 遗产通假设展示量（每 DAU 每日）

| 广告位 | 每 DAU 展示次数 | 说明 |
|--------|-----------------|------|
| 首页 Banner | 2 | 进入首页 1 次 + 切换 Tab 返回 1 次 |
| 案例详情原生 | 0.5 | 约 50% 用户会看案例 |
| AI 插屏 | 0.3 | 每 3 次对话 1 次，约 30% 用户触发 |
| 法条 Banner | 0.4 | 约 40% 用户会看法条 |
| 激励视频 | 0.1 | 假设 10% 用户观看解锁 |
| **合计** | **约 3.3 次/DAU** | |

### 7.3 日收入预估（取中等 eCPM）

| DAU | 日展示量（千次） | 日收入（¥） | 月收入（¥） |
|-----|------------------|-------------|-------------|
| 500 | 1.65 | 约 50–80 | 1,500–2,400 |
| 1,000 | 3.3 | 约 100–165 | 3,000–5,000 |
| 5,000 | 16.5 | 约 500–825 | 15,000–25,000 |
| 10,000 | 33 | 约 1,000–1,650 | 30,000–50,000 |

**计算说明**：  
- 展示量 = DAU × 3.3  
- 收入 = 展示量 × 混合 eCPM（按 ¥30–50/千次估算）

---

## 8. 注意事项

### 8.1 应用商店政策

| 商店 | 广告相关要求 |
|------|--------------|
| 华为 | 需在应用描述中说明含广告，不得误导用户 |
| 小米 | 广告需可关闭，不得强制点击 |
| OPPO / vivo | 同上，不得干扰正常使用 |
| 应用宝 | 需符合腾讯广告规范，隐私政策中披露 SDK |

### 8.2 用户体验平衡

- **新用户**：前 7 天减少或关闭插屏，仅保留 Banner  
- **频次**：插屏间隔 ≥ 2 分钟，单次启动 ≤ 2 次  
- **场景**：不在法律咨询、输入等关键操作时弹出插屏  
- **标识**：所有广告需明确标注「广告」字样  

### 8.3 法律类 App 合规

- 广告内容不得涉及「保证胜诉」「包打赢」等承诺  
- 不得在法条、判决等核心内容中插入误导性广告  
- 隐私政策中说明：为展示广告会收集设备信息、粗略位置等  

### 8.4 禁止行为

- 不得诱导或强制用户点击广告  
- 不得使用测试广告位 ID 上线  
- 不得自己点击自己的广告（会导致封号）  

---

## 9. 行动清单

### 今天可做（Phase 0）

- [ ] 注册穿山甲账号：https://www.csjplatform.com  
- [ ] 注册优量汇账号：https://adnet.qq.com/register  
- [ ] 确认软著、应用商店上架材料齐全  
- [ ] **不要**修改 `remote_config.json` 的广告开关  
- [ ] **不要**添加任何广告 SDK  

### 日活 100–500 时（Phase 1）

- [ ] 在穿山甲后台创建「遗产通」应用并提交审核  
- [ ] 创建 Banner、插屏、激励、原生广告位  
- [ ] 记录各广告位的正式 ID 和测试 ID  
- [ ] 在优量汇完成同样步骤  

### 日活 1000+ 时（Phase 2）

- [ ] 在 `build.gradle` 中添加穿山甲 SDK 依赖  
- [ ] 实现 `RemoteConfig` 扩展方法（placements、provider）  
- [ ] 在首页底部加入 Banner 容器并实现加载逻辑  
- [ ] 更新 `remote_config.json` schema，保持 `enabled: false`  
- [ ] 测试通过后，将 `home_banner.enabled` 设为 `true` 灰度发布  

### 日活 5000+ 时（Phase 3）

- [ ] 接入优量汇作为备选网络  
- [ ] 在案例详情、法条详情、AI 对话后增加对应广告位  
- [ ] 配置频次控制与新用户策略  
- [ ] 根据数据调整广告位与形式  

---

## 附录：参考链接

| 资源 | 链接 |
|------|------|
| 穿山甲开发者平台 | https://www.csjplatform.com |
| 穿山甲 SDK 下载与接入 | https://www.csjplatform.com/union/media/union/download/pangle |
| 优量汇注册 | https://adnet.qq.com/register |
| 腾讯广告联盟文档 | https://e.qq.com/dev |
| 遗产通 RemoteConfig 默认 URL | https://raw.githubusercontent.com/blueleafdi2/YiChanTong/main/remote_config.json |

---

*文档由王迪维护，如有更新请同步修改本文档。*
