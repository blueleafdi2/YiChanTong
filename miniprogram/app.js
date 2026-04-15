var analytics = require('./utils/analytics')
var remoteConfig = require('./utils/remote-config')
var adManager = require('./utils/ad-manager')

App({
  onLaunch: function () {
    analytics.log('app_launch')
    this.loadData()
    this.checkPrivacyConsent()
    remoteConfig.load(function (cfg) {
      if (cfg && cfg.adConfig) {
        adManager.updateConfig({
          enabled: cfg.adEnabled || false,
          bannerAdUnitId: (cfg.adConfig || {}).bannerAdUnitId || '',
          interstitialAdUnitId: (cfg.adConfig || {}).interstitialAdUnitId || '',
          rewardedAdUnitId: (cfg.adConfig || {}).rewardedAdUnitId || '',
          bannerPages: (cfg.adConfig || {}).bannerPages || [],
          interstitialFrequency: (cfg.adConfig || {}).interstitialFrequency || 5,
          minSessionMinutes: (cfg.adConfig || {}).minSessionMinutes || 3
        })
      }
    })
  },

  checkPrivacyConsent() {
    try {
      var agreed = wx.getStorageSync('yct_privacy_agreed')
      if (agreed) return
    } catch (e) {}
    wx.showModal({
      title: '用户协议与隐私政策',
      content: '欢迎使用「遗产通」（深圳市迪莹互联网技术有限公司）！\n\n'
        + '• 收藏、笔记、浏览历史仅存储在您的设备本地\n'
        + '• AI法律助手基于深度合成技术，输出仅供参考\n'
        + '• 请勿输入真实姓名、身份证号等敏感个人信息\n'
        + '• 我们不会向第三方出售您的个人信息\n'
        + '• 本小程序不提供法律代理服务\n\n'
        + '继续使用即表示您同意以上条款。',
      confirmText: '同意',
      cancelText: '查看详情',
      confirmColor: '#2B5F8A',
      success: function (res) {
        if (res.confirm) {
          try { wx.setStorageSync('yct_privacy_agreed', '1') } catch (e) {}
        }
      }
    })
  },

  globalData: {
    laws: [],
    topics: [],
    faq: [],
    glossary: [],
    tools: [],
    cases: [],
    dataReady: false
  },

  loadData() {
    try {
      this.globalData.laws = require('./data/laws')
      this.globalData.topics = require('./data/topics')
      this.globalData.faq = require('./data/faq')
      this.globalData.glossary = require('./data/glossary')
      this.globalData.tools = require('./data/tools')
      this.globalData.cases = require('./data/cases')
      this.globalData.dataReady = true
      console.log('[DataLoad] laws:', this.globalData.laws.length,
        'cases:', this.globalData.cases.length,
        'faq:', this.globalData.faq.length,
        'topics:', this.globalData.topics.length,
        'glossary:', this.globalData.glossary.length,
        'tools:', this.globalData.tools.length)
    } catch (e) {
      console.error('[DataLoad] FAILED:', e)
    }
  },

  getLawById(id) {
    return this.globalData.laws.find(l => l.id === id || l.id === String(id))
  },

  getCaseById(id) {
    return this.globalData.cases.find(c => c.id === id || c.id === String(id))
  },

  getTopicById(id) {
    return this.globalData.topics.find(t => t.id === id)
  },

  getFaqById(id) {
    return this.globalData.faq.find(f => f.id === id)
  },

  getGlossaryTerm(term) {
    return this.globalData.glossary.find(g => g.term === term)
  },

  getToolById(id) {
    return this.globalData.tools.find(t => t.id === id)
  },

  search(query) {
    if (!query || query.length < 2) return []
    const q = query.toLowerCase()
    const results = []

    this.globalData.laws.forEach(item => {
      if ((item.title && item.title.toLowerCase().includes(q)) ||
          (item.plainExplanation && item.plainExplanation.toLowerCase().includes(q)) ||
          (item.originalText && item.originalText.toLowerCase().includes(q)) ||
          (item.keywords && item.keywords.some(k => k.toLowerCase().includes(q)))) {
        results.push({
          type: 'law', id: item.id,
          title: '第' + item.article + ' ' + item.title,
          subtitle: item.plainExplanation ? item.plainExplanation.substring(0, 60) + '...' : ''
        })
      }
    })

    this.globalData.cases.forEach(item => {
      if ((item.title && item.title.toLowerCase().includes(q)) ||
          (item.caseSummary && item.caseSummary.toLowerCase().includes(q)) ||
          (item.tags && item.tags.some(t => t.toLowerCase().includes(q)))) {
        results.push({
          type: 'case', id: item.id,
          title: item.title,
          subtitle: (item.court || '') + ' · ' + (item.judgeDate || '')
        })
      }
    })

    this.globalData.faq.forEach(item => {
      if (item.question.toLowerCase().includes(q) || item.answer.toLowerCase().includes(q)) {
        results.push({
          type: 'faq', id: item.id,
          title: item.question,
          subtitle: item.answer.substring(0, 60) + '...'
        })
      }
    })

    this.globalData.topics.forEach(item => {
      if (item.title.toLowerCase().includes(q) || item.description.toLowerCase().includes(q) ||
          (item.content && item.content.toLowerCase().includes(q))) {
        results.push({
          type: 'topic', id: item.id,
          title: item.title,
          subtitle: item.description
        })
      }
    })

    this.globalData.glossary.forEach(item => {
      if (item.term.toLowerCase().includes(q) || item.definition.toLowerCase().includes(q)) {
        results.push({
          type: 'glossary', id: item.term,
          title: item.term,
          subtitle: item.definition.substring(0, 60) + '...'
        })
      }
    })

    this.globalData.tools.forEach(item => {
      if (item.title.toLowerCase().includes(q) || item.description.toLowerCase().includes(q)) {
        results.push({
          type: 'tool', id: item.id,
          title: item.title,
          subtitle: item.description
        })
      }
    })

    return results
  }
})
