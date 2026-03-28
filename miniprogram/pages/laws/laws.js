const analytics = require('../../utils/analytics')

function uniqueChapters(laws) {
  const seen = new Set()
  const list = []
  ;(laws || []).forEach((l) => {
    const c = l.chapter || ''
    if (c && !seen.has(c)) {
      seen.add(c)
      list.push(c)
    }
  })
  return list
}

Page({
  data: {
    tabs: ['全部'],
    tabIndex: 0,
    lawList: []
  },

  _allLaws: [],

  onLoad() {
    const app = getApp()
    this._allLaws = app.globalData.laws || []
    const chapters = uniqueChapters(this._allLaws)
    this.setData({
      tabs: ['全部'].concat(chapters)
    })
    this.applyFilter(0)
  },

  onShow() {
    analytics.logScreen('laws')
  },

  applyFilter(tabIndex) {
    const tab = this.data.tabs[tabIndex]
    let list = this._allLaws
    if (tab && tab !== '全部') {
      list = list.filter((l) => l.chapter === tab)
    }
    const lawList = list.map((l) => {
      const kw = l.keywords || []
      return Object.assign({}, l, {
        articleLabel: `第${l.id}条`,
        tagPreview: kw.slice(0, 2)
      })
    })
    this.setData({ tabIndex, lawList })
  },

  onTabTap(e) {
    const { index } = e.currentTarget.dataset
    const i = Number(index)
    if (i === this.data.tabIndex) return
    this.applyFilter(i)
  },

  onLawTap(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    wx.navigateTo({ url: `/subpkg/law-detail/law-detail?id=${id}` })
  }
})
