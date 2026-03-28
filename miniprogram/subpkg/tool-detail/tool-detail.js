const analytics = require('../../utils/analytics')
const favorites = require('../../utils/favorites')
const lawLink = require('../../utils/law-link')

function buildContentBlocks(content) {
  const lines = (content || '').split('\n')
  const blocks = []
  let buf = []
  const flush = () => {
    if (!buf.length) return
    const text = buf.join('\n')
    if (text.trim()) {
      blocks.push({ type: 'text', parts: lawLink.parseLawRefs(text) })
    }
    buf = []
  }
  lines.forEach((line) => {
    if (/^#{1,3}\s+/.test(line)) {
      flush()
      const level = line.match(/^(#+)/)[1].length
      const text = line.replace(/^#+\s+/, '')
      blocks.push({ type: 'h', level, text })
    } else {
      buf.push(line)
    }
  })
  flush()
  return blocks.length ? blocks : [{ type: 'text', parts: lawLink.parseLawRefs(content || '') }]
}

Page({
  data: {
    tool: null,
    contentBlocks: [],
    notFound: false
  },

  onLoad(options) {
    const id = options.id || ''
    const app = getApp()
    const tool = (app.globalData.tools || []).find((t) => t.id === id)
    if (!tool) {
      this.setData({ notFound: true })
      wx.setNavigationBarTitle({ title: '未找到' })
      return
    }
    wx.setNavigationBarTitle({ title: tool.title })
    favorites.addHistory('tool', id, tool.title)
    analytics.logContentView('tool', id)
    this.setData({
      tool,
      contentBlocks: buildContentBlocks(tool.content || '')
    })
  },

  onShow() {
    analytics.logScreen('tool_detail')
  },

  onLawRefTap(e) {
    const { lawid } = e.currentTarget.dataset
    if (lawid) lawLink.navigateToLaw(lawid)
  },

  onExport() {
    const { tool } = this.data
    if (!tool || !tool.content) return
    var self = this
    wx.showActionSheet({
      itemList: ['复制全文', '导出文件并分享'],
      success: function (res) {
        if (res.tapIndex === 0) {
          wx.setClipboardData({
            data: tool.content,
            success: function () { wx.showToast({ title: '已复制到剪贴板', icon: 'success' }) }
          })
        } else if (res.tapIndex === 1) {
          self._exportFile()
        }
      }
    })
  },

  _exportFile() {
    var tool = this.data.tool
    if (!tool) return

    var lines = (tool.content || '').split('\n')
    var bodyHtml = ''
    lines.forEach(function (line) {
      if (/^### /.test(line)) {
        bodyHtml += '<h3>' + line.replace(/^### /, '') + '</h3>\n'
      } else if (/^## /.test(line)) {
        bodyHtml += '<h2>' + line.replace(/^## /, '') + '</h2>\n'
      } else if (/^# /.test(line)) {
        bodyHtml += '<h1>' + line.replace(/^# /, '') + '</h1>\n'
      } else if (line.trim()) {
        bodyHtml += '<p>' + line + '</p>\n'
      }
    })

    var html = '<!DOCTYPE html><html><head><meta charset="utf-8">'
      + '<style>body{font-family:"Microsoft YaHei","SimSun",sans-serif;max-width:700px;margin:0 auto;padding:20px;line-height:1.8;font-size:14px}'
      + 'h1{font-size:22px;color:#2B5F8A;border-bottom:2px solid #2B5F8A;padding-bottom:8px}'
      + 'h2{font-size:18px;color:#1A3D5C;margin-top:24px}'
      + 'h3{font-size:16px;color:#333;margin-top:16px}'
      + 'p{margin:8px 0;text-indent:0}'
      + '.footer{margin-top:40px;font-size:12px;color:#999;text-align:center}</style></head><body>'
      + '<h1>' + tool.title + '</h1>\n'
      + bodyHtml
      + '<div class="footer">由「遗产通」小程序导出 · 仅供参考，不构成法律意见</div>'
      + '</body></html>'

    var fs = wx.getFileSystemManager()
    var fileName = tool.title.replace(/[/\\:*?"<>|]/g, '') + '.html'
    var filePath = wx.env.USER_DATA_PATH + '/' + fileName

    try {
      fs.writeFileSync(filePath, html, 'utf8')
      wx.shareFileMessage({
        filePath: filePath,
        fileName: fileName,
        success: function () {
          wx.showToast({ title: '分享成功', icon: 'success' })
        },
        fail: function () {
          wx.openDocument({
            filePath: filePath,
            fileType: 'html',
            showMenu: true,
            success: function () {},
            fail: function () {
              wx.showToast({ title: '请使用复制全文功能', icon: 'none' })
            }
          })
        }
      })
      analytics.log('tool_export', { id: tool.id, format: 'html' })
    } catch (e) {
      wx.setClipboardData({
        data: tool.content,
        success: function () { wx.showToast({ title: '已复制到剪贴板', icon: 'success' }) }
      })
    }
  }
})
