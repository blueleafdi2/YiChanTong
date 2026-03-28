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
    analytics.logScreen('history')
    this.refresh()
  },

  refresh() {
    const raw = favorites.getHistory()
    const list = raw.map((i) => ({
      ...i,
      uid: `${i.type}_${i.id}_${i.time}`,
      timeLabel: fmtTime(i.time)
    }))
    this.setData({ list })
  },

  onRowTap(e) {
    const { index } = e.currentTarget.dataset
    const row = this.data.list[index]
    if (row) goRecord(row)
  },

  onClear() {
    wx.showModal({
      title: '清空浏览历史',
      content: '确定要清空全部历史记录吗？',
      confirmColor: '#B33A3A',
      success: (res) => {
        if (res.confirm) {
          favorites.clearHistory()
          analytics.log('history_clear')
          this.refresh()
          wx.showToast({ title: '已清空', icon: 'success' })
        }
      }
    })
  }
})
