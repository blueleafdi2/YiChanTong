const analytics = require('../../utils/analytics')
const favorites = require('../../utils/favorites')

function fmtTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const p = (n) => (n < 10 ? `0${n}` : `${n}`)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

function goRecord(item) {
  const { type, id } = item
  if (!type || id == null || id === '') return
  const enc = encodeURIComponent(String(id))
  if (type === 'law') wx.navigateTo({ url: `/subpkg/law-detail/law-detail?id=${enc}` })
  else if (type === 'case') wx.navigateTo({ url: `/subpkg/case-detail/case-detail?id=${enc}` })
  else if (type === 'topic') wx.navigateTo({ url: `/subpkg/topic-detail/topic-detail?topic_id=${enc}` })
  else if (type === 'faq') wx.navigateTo({ url: `/subpkg/topic-detail/topic-detail?faq_id=${enc}` })
  else if (type === 'tool') wx.navigateTo({ url: `/subpkg/tool-detail/tool-detail?id=${enc}` })
  else wx.showToast({ title: '暂不支持打开', icon: 'none' })
}

Page({
  data: {
    list: []
  },

  onLoad() {
    const sys = wx.getSystemInfoSync()
    this._pxToRpx = 750 / (sys.windowWidth || 375)
  },

  onShow() {
    analytics.logScreen('favorites')
    this.refresh()
  },

  refresh() {
    const raw = favorites.getFavorites()
    const list = raw.map((i) =>
      Object.assign({}, i, {
        uid: `${i.type}_${i.id}`,
        slideX: 0,
        timeLabel: fmtTime(i.time)
      })
    )
    this.setData({ list })
  },

  onTouchStart(e) {
    const { index } = e.currentTarget.dataset
    this._touch = { index, x0: e.touches[0].clientX }
    this._touchMoved = false
  },

  onTouchMove(e) {
    if (this._touch == null || this._touch.index !== e.currentTarget.dataset.index) return
    const dx = e.touches[0].clientX - this._touch.x0
    if (Math.abs(dx) > 12) this._touchMoved = true
    const ratio = this._pxToRpx || 2
    const dxRpx = dx * ratio
    const slideX = Math.min(0, Math.max(-168, dxRpx))
    const key = `list[${this._touch.index}].slideX`
    this.setData({ [key]: slideX })
  },

  onTouchEnd(e) {
    const { index } = e.currentTarget.dataset
    if (this._touch == null) return
    const row = this.data.list[index]
    if (!row) return
    const snap = row.slideX < -56 ? -168 : 0
    const list = this.data.list.map((r, i) =>
      Object.assign({}, r, { slideX: i === index ? snap : 0 })
    )
    this.setData({ list })
    this._touch = null
  },

  onRowTap(e) {
    const { index } = e.currentTarget.dataset
    const row = this.data.list[index]
    if (!row) return
    if (row.slideX < -56) {
      const list = this.data.list.map((r, i) =>
        Object.assign({}, r, { slideX: i === index ? 0 : r.slideX })
      )
      this.setData({ list })
      return
    }
    if (this._touchMoved) return
    goRecord(row)
  },

  onDelete(e) {
    const { type, id } = e.currentTarget.dataset
    favorites.removeFavorite(type, id)
    analytics.log('favorite_remove', { type, id })
    this.refresh()
  }
})
