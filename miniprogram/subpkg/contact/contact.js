var analytics = require('../../utils/analytics')

var HOTLINE = '12348'
var EMAIL = '775035298@qq.com'

Page({
  data: {
    hotline: HOTLINE,
    email: EMAIL
  },

  onShow: function () {
    analytics.logScreen('contact')
  },

  onCallHotline: function () {
    wx.makePhoneCall({ phoneNumber: HOTLINE })
  },

  onCopyEmail: function () {
    wx.setClipboardData({
      data: EMAIL,
      success: function () { wx.showToast({ title: '邮箱已复制', icon: 'success' }) }
    })
  }
})
