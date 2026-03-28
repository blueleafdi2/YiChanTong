const analytics = require('../../utils/analytics')

const QUICK_CHIPS = [
  { label: '遗嘱效力', topicId: 'topic_02' },
  { label: '法定继承', topicId: 'topic_01' },
  { label: '遗产范围', topicId: 'topic_04' },
  { label: '代位继承', topicId: 'topic_06' },
  { label: '遗产分割', topicId: 'topic_07' }
]

function getDayOfYear() {
  const now = new Date()
  const start = new Date(now.getFullYear(), 0, 0)
  return Math.floor((now - start) / 86400000)
}

Page({
  data: {
    statusBarHeight: 44,
    bannerPaddingTop: 88,
    quickChips: QUICK_CHIPS,
    faqList: [],
    topicGrid: [],
    todayCase: null
  },

  onLoad() {
    const sys = wx.getSystemInfoSync()
    let bannerPaddingTop = sys.statusBarHeight + 12
    try {
      const menu = wx.getMenuButtonBoundingClientRect()
      if (menu && menu.top) {
        bannerPaddingTop = menu.top + menu.height + 24
      }
    } catch (e) {
      bannerPaddingTop = sys.statusBarHeight + 88
    }
    this.setData({
      statusBarHeight: sys.statusBarHeight,
      bannerPaddingTop
    })
    this.refreshContent()
  },

  onShow() {
    analytics.logScreen('home')
    this.refreshContent()
  },

  refreshContent() {
    const app = getApp()
    const gd = app.globalData
    const faq = (gd.faq || []).slice(0, 5)
    const topicGrid = (gd.topics || []).slice(0, 6)
    const cases = gd.cases || []
    const len = cases.length
    let todayCase = null
    if (len > 0) {
      const idx = getDayOfYear() % len
      const raw = cases[idx]
      todayCase = Object.assign({}, raw, {
        displayTags: (raw.tags || []).slice(0, 3)
      })
    }
    this.setData({
      faqList: faq,
      topicGrid,
      todayCase
    })
  },

  onTapSearch() {
    wx.navigateTo({ url: '/pages/search/search' })
  },

  onTapChip(e) {
    const { topicid } = e.currentTarget.dataset
    if (!topicid) return
    wx.navigateTo({ url: `/subpkg/topic-detail/topic-detail?topic_id=${topicid}` })
  },

  onTapTodayCase() {
    const c = this.data.todayCase
    if (!c || !c.id) return
    wx.navigateTo({ url: `/subpkg/case-detail/case-detail?id=${c.id}` })
  },

  onTapFaq(e) {
    const item = e.currentTarget.dataset
    if (item.topicid) {
      wx.navigateTo({ url: `/subpkg/topic-detail/topic-detail?topic_id=${item.topicid}` })
    } else if (item.faqid) {
      wx.navigateTo({ url: `/subpkg/topic-detail/topic-detail?faq_id=${item.faqid}` })
    }
  },

  onTapTopic(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    wx.navigateTo({ url: `/subpkg/topic-detail/topic-detail?topic_id=${id}` })
  }
})
