const analytics = require('../../utils/analytics')

const BADGE_LABELS = {
  law: '法条',
  case: '案例',
  faq: 'FAQ',
  topic: '专题',
  glossary: '术语',
  tool: '工具'
}

const HOT_KEYWORDS = [
  '遗嘱',
  '法定继承',
  '代位继承',
  '遗产分配',
  '丧失继承权',
  '遗赠扶养协议',
  '遗产管理人',
  '打印遗嘱'
]

function decorateResults(raw) {
  return (raw || []).map((r) => ({
    ...r,
    badgeLabel: BADGE_LABELS[r.type] || r.type
  }))
}

Page({
  data: {
    query: '',
    results: [],
    showHot: true,
    showEmpty: false,
    autoFocus: true,
    hotKeywords: HOT_KEYWORDS
  },

  _debounceTimer: null,

  onLoad() {
  },

  onShow() {
    analytics.logScreen('search')
  },

  onUnload() {
    if (this._debounceTimer) {
      clearTimeout(this._debounceTimer)
      this._debounceTimer = null
    }
  },

  runSearch(rawQuery) {
    const app = getApp()
    const q = (rawQuery || '').trim()
    if (q.length < 2) {
      this.setData({ results: [], showEmpty: false })
      return
    }
    const raw = app.search(q) || []
    analytics.logSearch(q, raw.length)
    const results = decorateResults(raw)
    this.setData({
      results,
      showEmpty: raw.length === 0,
      showHot: false
    })
  },

  onQueryInput(e) {
    const query = e.detail.value || ''
    const trimmed = query.trim()
    this.setData({
      query,
      showHot: !trimmed
    })
    if (this._debounceTimer) clearTimeout(this._debounceTimer)
    this._debounceTimer = setTimeout(() => {
      this._debounceTimer = null
      if (!trimmed) {
        this.setData({ results: [], showEmpty: false })
        return
      }
      if (trimmed.length < 2) {
        this.setData({ results: [], showEmpty: false })
        return
      }
      this.runSearch(query)
    }, 300)
  },

  onQueryConfirm() {
    const q = (this.data.query || '').trim()
    if (q.length < 2) {
      wx.showToast({ title: '至少输入2个字', icon: 'none' })
      return
    }
    if (this._debounceTimer) {
      clearTimeout(this._debounceTimer)
      this._debounceTimer = null
    }
    this.setData({ showHot: false })
    this.runSearch(q)
  },

  onHotTap(e) {
    const kw = e.currentTarget.dataset.kw
    if (!kw) return
    if (this._debounceTimer) {
      clearTimeout(this._debounceTimer)
      this._debounceTimer = null
    }
    this.setData({ query: kw, showHot: false })
    this.runSearch(kw)
  },

  onResultTap(e) {
    const { type, id } = e.currentTarget.dataset
    if (!type) return

    if (type === 'glossary') {
      const term = id
      const app = getApp()
      const row = (app.globalData.glossary || []).find((x) => x.term === term)
      let content = row ? row.definition : '暂无该术语的详细释义。'
      if (content.length > 900) {
        content = content.slice(0, 897) + '...'
      }
      wx.showModal({
        title: term,
        content,
        showCancel: false,
        confirmText: '我知道了',
        confirmColor: '#2B5F8A'
      })
      return
    }

    const enc = encodeURIComponent(String(id))
    if (type === 'law') {
      wx.navigateTo({ url: `/subpkg/law-detail/law-detail?id=${enc}` })
      return
    }
    if (type === 'case') {
      wx.navigateTo({ url: `/subpkg/case-detail/case-detail?id=${enc}` })
      return
    }
    if (type === 'faq') {
      wx.navigateTo({ url: `/subpkg/topic-detail/topic-detail?faq_id=${enc}` })
      return
    }
    if (type === 'topic') {
      wx.navigateTo({ url: `/subpkg/topic-detail/topic-detail?topic_id=${enc}` })
      return
    }
    if (type === 'tool') {
      wx.navigateTo({ url: '/subpkg/tool-detail/tool-detail?id=' + enc })
    }
  },

  onEmptyAiTap: function () {
    var q = (this.data.query || '').trim()
    wx.navigateTo({ url: '/subpkg/ai/ai' })
  },

  onShareAppMessage: function () {
    return {
      title: '遗产通 — 继承法知识搜索',
      path: '/pages/search/search'
    }
  }
})
