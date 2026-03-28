const analytics = require('../../utils/analytics')

const PRIVACY_TEXT =
  '「遗产通」小程序尊重并保护用户隐私。\n\n' +
  '1. 本小程序主要在本地展示法律知识与工具，收藏、笔记、浏览历史等数据仅存储于您的设备本地。\n' +
  '2. 我们不会向第三方出售您的个人信息。\n' +
  '3. 请勿在小程序中输入真实姓名、身份证号等敏感个人信息。\n' +
  '4. 本说明仅供一般说明，不构成法律意见。'

Page({
  data: {},

  onShow() {
    analytics.logScreen('mine')
  },

  onMenuTap(e) {
    const url = e.currentTarget.dataset.url
    if (!url) return
    wx.navigateTo({ url })
  },

  onPrivacyTap() {
    wx.showModal({
      title: '隐私政策',
      content: PRIVACY_TEXT,
      showCancel: false,
      confirmText: '我知道了',
      confirmColor: '#2B5F8A'
    })
  }
})
