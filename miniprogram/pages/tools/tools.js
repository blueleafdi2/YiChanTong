const analytics = require('../../utils/analytics')

Page({
  data: {
    tools: []
  },

  onShow() {
    analytics.logScreen('tools')
  },

  onLoad() {
    const app = getApp()
    this.setData({ tools: app.globalData.tools || [] })
  },

  onGlossaryTap() {
    wx.navigateTo({ url: '/subpkg/glossary/glossary' })
  },

  onToolTap(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({ url: `/subpkg/tool-detail/tool-detail?id=${id}` })
  },

})
