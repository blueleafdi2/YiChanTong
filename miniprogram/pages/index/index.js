var analytics = require('../../utils/analytics')

var QUICK_CHIPS = [
  { label: '遗嘱效力', topicId: 'topic_02' },
  { label: '法定继承', topicId: 'topic_01' },
  { label: '遗产范围', topicId: 'topic_04' },
  { label: '代位继承', topicId: 'topic_06' },
  { label: '遗产分割', topicId: 'topic_07' }
]

var NEWS_ITEMS = [
  { title: '2026年起非公证继承告知承诺制全面落地', url: '' }
]

function getDayOfYear() {
  var now = new Date()
  var start = new Date(now.getFullYear(), 0, 0)
  return Math.floor((now - start) / 86400000)
}

Page({
  data: {
    statusBarHeight: 44,
    bannerPaddingTop: 88,
    quickChips: QUICK_CHIPS,
    faqList: [],
    topicGrid: [],
    featuredCases: [],
    newsItem: NEWS_ITEMS[0] || null
  },

  onLoad: function () {
    var sys = wx.getSystemInfoSync()
    var bannerPaddingTop = sys.statusBarHeight + 12
    try {
      var menu = wx.getMenuButtonBoundingClientRect()
      if (menu && menu.top) {
        bannerPaddingTop = menu.top + menu.height + 24
      }
    } catch (e) {
      bannerPaddingTop = sys.statusBarHeight + 88
    }
    this.setData({
      statusBarHeight: sys.statusBarHeight,
      bannerPaddingTop: bannerPaddingTop
    })
    this.refreshContent()
  },

  onShow: function () {
    analytics.logScreen('home')
    this.refreshContent()
  },

  refreshContent: function () {
    var app = getApp()
    var gd = app.globalData
    var faq = (gd.faq || []).slice(0, 3)
    var cases = gd.cases || []
    var len = cases.length

    var featuredCases = []
    if (len > 0) {
      var base = getDayOfYear()
      for (var i = 0; i < Math.min(3, len); i++) {
        var idx = (base + i) % len
        var raw = cases[idx]
        featuredCases.push(Object.assign({}, raw, {
          displayTags: (raw.tags || []).slice(0, 3)
        }))
      }
    }

    this.setData({
      faqList: faq,
      featuredCases: featuredCases
    })
  },

  onTapSearch: function () {
    wx.navigateTo({ url: '/pages/search/search' })
  },

  onTapAI: function () {
    wx.navigateTo({ url: '/subpkg/ai/ai' })
  },

  onQuickTap: function (e) {
    var tab = e.currentTarget.dataset.tab
    if (tab === 'laws') {
      wx.switchTab({ url: '/pages/laws/laws' })
    } else if (tab === 'cases') {
      wx.switchTab({ url: '/pages/cases/cases' })
    } else if (tab === 'tools') {
      wx.switchTab({ url: '/pages/tools/tools' })
    } else if (tab === 'glossary') {
      wx.navigateTo({ url: '/subpkg/glossary/glossary' })
    }
  },

  onTapChip: function (e) {
    var topicid = e.currentTarget.dataset.topicid
    if (!topicid) return
    wx.navigateTo({ url: '/subpkg/topic-detail/topic-detail?topic_id=' + topicid })
  },

  onTapCase: function (e) {
    var id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({ url: '/subpkg/case-detail/case-detail?id=' + id })
  },

  onTapFaq: function (e) {
    var item = e.currentTarget.dataset
    if (item.topicid) {
      wx.navigateTo({ url: '/subpkg/topic-detail/topic-detail?topic_id=' + item.topicid })
    } else if (item.faqid) {
      wx.navigateTo({ url: '/subpkg/topic-detail/topic-detail?faq_id=' + item.faqid })
    }
  },

  onTapNews: function () {
    // Future: navigate to news detail or external link
  },

  onShareAppMessage: function () {
    return {
      title: '遗产通 — 让继承法触手可及',
      path: '/pages/index/index'
    }
  }
})
