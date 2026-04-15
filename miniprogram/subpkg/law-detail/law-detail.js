const analytics = require('../../utils/analytics')
const favorites = require('../../utils/favorites')

function mergeHighlightRanges(ranges) {
  if (!ranges.length) return []
  ranges.sort((a, b) => a.s - b.s)
  const out = [{ s: ranges[0].s, e: ranges[0].e }]
  for (let i = 1; i < ranges.length; i++) {
    const r = ranges[i]
    const last = out[out.length - 1]
    if (r.s < last.e) last.e = Math.max(last.e, r.e)
    else out.push({ s: r.s, e: r.e })
  }
  return out
}

function plainSegments(text, keywords) {
  if (!text) return []
  const kws = [...new Set((keywords || []).filter(Boolean))].sort((a, b) => b.length - a.length)
  if (!kws.length) return [{ text, highlight: false }]
  const occ = []
  kws.forEach((kw) => {
    let start = 0
    let idx
    while ((idx = text.indexOf(kw, start)) !== -1) {
      occ.push({ s: idx, e: idx + kw.length })
      start = idx + 1
    }
  })
  if (!occ.length) return [{ text, highlight: false }]
  const merged = mergeHighlightRanges(occ)
  const parts = []
  let p = 0
  merged.forEach((m) => {
    if (m.s > p) parts.push({ text: text.slice(p, m.s), highlight: false })
    parts.push({ text: text.slice(m.s, m.e), highlight: true })
    p = m.e
  })
  if (p < text.length) parts.push({ text: text.slice(p), highlight: false })
  return parts
}

Page({
  data: {
    law: null,
    plainSegments: [],
    favorited: false,
    notFound: false
  },

  _lawId: '',

  onLoad(options) {
    const id = options.id || ''
    this._lawId = id
    const app = getApp()
    const law = (app.globalData.laws || []).find((l) => l.id === id || String(l.id) === String(id))
    if (!law) {
      var n = parseInt(id, 10)
      var msg = '未找到该法条'
      if (n && (n < 1119 || n > 1163)) {
        msg = '第' + id + '条不在继承编范围（第1119-1163条）\n本库仅收录民法典继承编内容'
      }
      this.setData({ notFound: true, notFoundMsg: msg })
      wx.setNavigationBarTitle({ title: '未找到' })
      return
    }
    const title = `${law.article} ${law.title}`
    wx.setNavigationBarTitle({ title: law.title || '法条详情' })
    favorites.addHistory('law', id, title)
    analytics.logContentView('law', id)
    this.setData({
      law,
      plainSegments: plainSegments(law.plainExplanation || '', law.keywords || []),
      favorited: favorites.isFavorited('law', id)
    })
  },

  onShow() {
    analytics.logScreen('law_detail')
    if (this._lawId) {
      this.setData({ favorited: favorites.isFavorited('law', this._lawId) })
    }
  },

  onToggleFavorite() {
    const { law } = this.data
    if (!law) return
    const title = `${law.article} ${law.title}`
    if (this.data.favorited) {
      favorites.removeFavorite('law', law.id)
      this.setData({ favorited: false })
    } else {
      favorites.addFavorite('law', law.id, title)
      this.setData({ favorited: true })
      analytics.logFavorite('law', law.id)
    }
  },

  onAddNote() {
    const { law } = this.data
    if (!law) return
    const existing = favorites.getNote('law', law.id)
    const self = this
    wx.showModal({
      title: '记笔记',
      editable: true,
      placeholderText: '输入您的笔记内容…',
      content: existing ? existing.content : '',
      confirmText: '保存',
      confirmColor: '#2B5F8A',
      success: function (res) {
        if (res.confirm && res.content && res.content.trim()) {
          const title = law.article + ' ' + law.title
          favorites.saveNote('law', law.id, title, res.content.trim())
          wx.showToast({ title: '笔记已保存', icon: 'success' })
          analytics.log('note_save', { type: 'law', id: law.id })
        }
      }
    })
  },

  onShareAppMessage: function () {
    var law = this.data.law
    return {
      title: law ? ('第' + law.article + '条 ' + law.title) : '遗产通 — 法条详情',
      path: '/subpkg/law-detail/law-detail?id=' + (law ? law.id : '')
    }
  }
})
