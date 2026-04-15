var analytics = require('../../utils/analytics')

var HOTLINE = '12348'
var EMAIL = '775035298@qq.com'
var WECHAT_ID = 'LawServicePro'

Page({
  data: {
    hotline: HOTLINE,
    email: EMAIL,
    wechatId: WECHAT_ID,
    hasQrCode: true
  },

  onShow: function () {
    analytics.logScreen('contact_lawyer')
  },

  onCopyWechat: function () {
    wx.setClipboardData({
      data: WECHAT_ID,
      success: function () {
        wx.showToast({ title: '微信号已复制', icon: 'success' })
      }
    })
  },

  onCallHotline: function () {
    wx.makePhoneCall({ phoneNumber: HOTLINE })
  },

  onCopyEmail: function () {
    wx.setClipboardData({
      data: EMAIL,
      success: function () {
        wx.showToast({ title: '邮箱已复制', icon: 'success' })
      }
    })
  },

  onTapAI: function () {
    wx.navigateTo({ url: '/subpkg/ai/ai' })
  },

  onShareAppMessage: function () {
    return {
      title: '遗产通 — 专业继承法律咨询',
      path: '/subpkg/contact/contact'
    }
  }
})
