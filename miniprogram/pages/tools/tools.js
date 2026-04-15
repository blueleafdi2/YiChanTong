var analytics = require('../../utils/analytics')

Page({
  data: {
    tools: []
  },

  onShow: function () {
    analytics.logScreen('tools')
  },

  onLoad: function () {
    var app = getApp()
    this.setData({ tools: app.globalData.tools || [] })
  },

  onGlossaryTap: function () {
    wx.navigateTo({ url: '/subpkg/glossary/glossary' })
  },

  onAiTap: function () {
    wx.navigateTo({ url: '/subpkg/ai/ai' })
  },

  onToolTap: function (e) {
    var id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({ url: '/subpkg/tool-detail/tool-detail?id=' + id })
  },

  onShareAppMessage: function () {
    return {
      title: '遗产通 — 继承法实用工具',
      path: '/pages/tools/tools'
    }
  }
})
