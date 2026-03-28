const analytics = require('../../utils/analytics')
const lawLink = require('../../utils/law-link')

const DIFF_BADGE = { easy: '✅', medium: '📝', hard: '⚠️' }

function uniqueCategories(list) {
  const s = new Set()
  ;(list || []).forEach((g) => {
    if (g.category) s.add(g.category)
  })
  return Array.from(s).sort()
}

function filterList(all, keyword, categoryFilter) {
  let rows = all.slice()
  const q = (keyword || '').trim().toLowerCase()
  if (q) {
    rows = rows.filter(
      (g) =>
        (g.term && g.term.toLowerCase().includes(q)) ||
        (g.definition && g.definition.toLowerCase().includes(q))
    )
  }
  if (categoryFilter === '__novice__') {
    rows = rows.filter((g) => g.difficulty === 'medium' || g.difficulty === 'hard')
  } else if (categoryFilter && categoryFilter !== '全部') {
    rows = rows.filter((g) => g.category === categoryFilter)
  }
  return rows.map((g) => ({
    ...g,
    badge: DIFF_BADGE[g.difficulty] || '•',
    preview: (g.definition || '').length > 72 ? `${(g.definition || '').slice(0, 72)}…` : g.definition || ''
  }))
}

Page({
  data: {
    search: '',
    categoryTabs: ['全部', '小白必看'],
    categoryFilter: '全部',
    list: [],
    allCount: 0
  },

  _all: [],

  onLoad() {
    const app = getApp()
    this._all = app.globalData.glossary || []
    const cats = uniqueCategories(this._all)
    this.setData({
      categoryTabs: ['全部', '小白必看'].concat(cats),
      allCount: this._all.length
    })
    this.applyFilter()
  },

  onShow() {
    analytics.logScreen('glossary')
  },

  applyFilter() {
    const { search, categoryFilter } = this.data
    const catKey = categoryFilter === '小白必看' ? '__novice__' : categoryFilter
    const list = filterList(this._all, search, catKey)
    this.setData({ list })
  },

  onSearchInput(e) {
    this.setData({ search: e.detail.value || '' })
    this.applyFilter()
  },

  onCategoryTap(e) {
    const { cat } = e.currentTarget.dataset
    if (!cat || cat === this.data.categoryFilter) return
    this.setData({ categoryFilter: cat })
    this.applyFilter()
  },

  onTermTap(e) {
    const { term } = e.currentTarget.dataset
    const g = this._all.find((x) => x.term === term)
    if (!g) return
    const lawId = lawLink.extractLawIdFromCitation(g.relatedLaw || '')
    const related = g.relatedLaw || '—'
    const example = g.example || '（暂无示例）'
    wx.showModal({
      title: g.term,
      content: `${g.definition}\n\n【示例】${example}\n\n【关联】${related}`,
      confirmText: lawId ? '看法条' : '知道了',
      cancelText: '关闭',
      success: (res) => {
        if (res.confirm && lawId) lawLink.navigateToLaw(lawId)
      }
    })
  }
})
