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

  onShow() {
    analytics.logScreen('notes')
    const raw = favorites.getNotes()
    const list = raw.map((i) => ({
      ...i,
      uid: `${i.type}_${i.id}`,
      timeLabel: fmtTime(i.time),
      preview: (i.content || '').length > 96 ? `${(i.content || '').slice(0, 96)}…` : i.content || ''
    }))
    this.setData({ list })
  },

  onRowTap(e) {
    const { index } = e.currentTarget.dataset
    const row = this.data.list[index]
    if (row) goRecord(row)
  }
})
