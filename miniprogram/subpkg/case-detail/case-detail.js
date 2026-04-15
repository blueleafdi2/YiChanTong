const analytics = require('../../utils/analytics')
const favorites = require('../../utils/favorites')
const lawLink = require('../../utils/law-link')

function mapLegalBasis(list) {
  return (list || []).map((text) => ({
    text,
    lawId: lawLink.extractLawIdFromCitation(text)
  }))
}

Page({
  data: {
    item: null,
    legalBasisItems: [],
    favorited: false,
    notFound: false
  },

  _caseId: '',

  onLoad(options) {
    const id = options.id || ''
    this._caseId = id
    const app = getApp()
    const item = (app.globalData.cases || []).find((c) => c.id === id || String(c.id) === String(id))
    if (!item) {
      this.setData({ notFound: true })
      wx.setNavigationBarTitle({ title: '未找到' })
      return
    }
    wx.setNavigationBarTitle({ title: '案例详情' })
    favorites.addHistory('case', id, item.title)
    analytics.logContentView('case', id)
    this.setData({
      item,
      legalBasisItems: mapLegalBasis(item.legalBasis),
      favorited: favorites.isFavorited('case', id)
    })
  },

  onShow() {
    analytics.logScreen('case_detail')
    if (this._caseId) {
      this.setData({ favorited: favorites.isFavorited('case', this._caseId) })
    }
  },

  onToggleFavorite() {
    const { item } = this.data
    if (!item) return
    if (this.data.favorited) {
      favorites.removeFavorite('case', item.id)
      this.setData({ favorited: false })
    } else {
      favorites.addFavorite('case', item.id, item.title)
      this.setData({ favorited: true })
      analytics.logFavorite('case', item.id)
    }
  },

  onLawBasisTap(e) {
    const { lawid } = e.currentTarget.dataset
    if (!lawid) return
    lawLink.navigateToLaw(lawid)
  },

  onOpenSource() {
    const { item } = this.data
    if (!item || !item.sourceUrl) return
    wx.setClipboardData({
      data: item.sourceUrl,
      success: function () { wx.showToast({ title: '链接已复制', icon: 'success' }) }
    })
  },

  onAddNote() {
    const { item } = this.data
    if (!item) return
    const existing = favorites.getNote('case', item.id)
    var self = this
    wx.showModal({
      title: '记笔记',
      editable: true,
      placeholderText: '输入您的笔记内容…',
      content: existing ? existing.content : '',
      confirmText: '保存',
      confirmColor: '#2B5F8A',
      success: function (res) {
        if (res.confirm && res.content && res.content.trim()) {
          favorites.saveNote('case', item.id, item.title, res.content.trim())
          wx.showToast({ title: '笔记已保存', icon: 'success' })
          analytics.log('note_save', { type: 'case', id: item.id })
        }
      }
    })
  },

  onShareAppMessage: function () {
    var item = this.data.item
    return {
      title: item ? item.title : '遗产通 — 案例详情',
      path: '/subpkg/case-detail/case-detail?id=' + (item ? item.id : '')
    }
  }
})
