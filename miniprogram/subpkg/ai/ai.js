const analytics = require('../../utils/analytics')
const lawLink = require('../../utils/law-link')

const DEEPSEEK_URL = 'https://api.deepseek.com/chat/completions'
const DEEPSEEK_KEY = 'YOUR_DEEPSEEK_API_KEY'
const STORAGE_KEY = 'yct_ai_chat'
const MAX_STORED = 50
const MAX_CONTEXT = 6

const SYSTEM_PROMPT =
  '你是遗产通AI助手，专注于中国继承法律咨询。请基于《中华人民共和国民法典》继承编（第1119-1163条）回答。回答中引用法条时使用格式「第XXXX条」。回答尽量简洁精炼，控制在300字以内。如果问题与继承法无关，简要回答后提示用户本助手专注继承法领域。'

function findCasesByArticles(cases, articleIds) {
  if (!articleIds || !articleIds.length) return []
  const ids = new Set(articleIds)
  const out = []
  ;(cases || []).forEach((c) => {
    const blob = (c.legalBasis || []).join(' ')
    for (const id of ids) {
      if (blob.includes(id) || blob.includes('第' + id + '条')) {
        out.push({ id: c.id, title: c.title })
        break
      }
    }
  })
  return out.slice(0, 6)
}

function fromStorage() {
  try {
    const raw = JSON.parse(wx.getStorageSync(STORAGE_KEY) || '[]')
    if (!Array.isArray(raw)) return []
    return raw.map((m) => {
      if (m.role === 'user') return { role: 'user', text: m.content || '' }
      const content = m.content || ''
      return {
        role: 'assistant',
        content,
        parts: lawLink.parseLawRefs(content),
        relatedLawIds: lawLink.extractLawIds(content),
        relatedCases: []
      }
    })
  } catch (e) {
    return []
  }
}

function toStorage(messages) {
  const flat = messages
    .filter((m) => !m.loading && m.role)
    .map((m) => ({
      role: m.role,
      content: m.role === 'user' ? m.text : m.content
    }))
  while (flat.length > MAX_STORED) flat.shift()
  try {
    wx.setStorageSync(STORAGE_KEY, JSON.stringify(flat))
  } catch (e) {}
}

function enrichRelated(messages, cases) {
  return messages.map((m) => {
    if (m.role !== 'assistant' || m.loading) return m
    const ids = m.relatedLawIds || lawLink.extractLawIds(m.content || '')
    return Object.assign({}, m, {
      relatedCases: findCasesByArticles(cases, ids)
    })
  })
}

Page({
  data: {
    messages: [],
    inputValue: '',
    sending: false,
    scrollTo: '',
    elapsedText: ''
  },

  _elapsedTimer: null,
  _startTime: 0,

  onLoad() {
    const app = getApp()
    const cases = app.globalData.cases || []
    const messages = enrichRelated(fromStorage(), cases)
    this.setData({ messages })
    this._scrollBottom()
  },

  onShow() {
    analytics.logScreen('ai_assistant')
  },

  onUnload() {
    this._stopTimer()
  },

  onInput(e) {
    this.setData({ inputValue: e.detail.value || '' })
  },

  _scrollBottom() {
    const n = this.data.messages.length
    if (!n) return
    this.setData({ scrollTo: 'msg-' + (n - 1) })
  },

  _startTimer() {
    this._startTime = Date.now()
    this.setData({ elapsedText: '' })
    this._elapsedTimer = setInterval(() => {
      const sec = Math.floor((Date.now() - this._startTime) / 1000)
      this.setData({ elapsedText: ' ' + sec + 's' })
    }, 1000)
  },

  _stopTimer() {
    if (this._elapsedTimer) {
      clearInterval(this._elapsedTimer)
      this._elapsedTimer = null
    }
    this.setData({ elapsedText: '' })
  },

  onLawRefTap(e) {
    const { lawid } = e.currentTarget.dataset
    if (lawid) lawLink.navigateToLaw(lawid)
  },

  onChipLaw(e) {
    const { id } = e.currentTarget.dataset
    if (id) lawLink.navigateToLaw(id)
  },

  onChipCase(e) {
    const { id } = e.currentTarget.dataset
    if (id) wx.navigateTo({ url: '/subpkg/case-detail/case-detail?id=' + id })
  },

  onSend() {
    const text = (this.data.inputValue || '').trim()
    if (!text || this.data.sending) return

    analytics.logAI(text)

    const userMsg = { role: 'user', text: text }
    const thinking = { role: 'assistant', loading: true, parts: [], content: '' }
    const messages = this.data.messages.concat([userMsg, thinking])
    this.setData({ messages: messages, inputValue: '', sending: true }, () => {
      this._scrollBottom()
    })
    this._startTimer()

    const recentHistory = messages
      .filter((m) => !m.loading)
      .map((m) => ({
        role: m.role,
        content: m.role === 'user' ? m.text : m.content
      }))
      .slice(-MAX_CONTEXT)

    const apiMessages = [{ role: 'system', content: SYSTEM_PROMPT }].concat(recentHistory)

    const self = this
    const app = getApp()

    wx.request({
      url: DEEPSEEK_URL,
      method: 'POST',
      timeout: 60000,
      header: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + DEEPSEEK_KEY
      },
      data: {
        model: 'deepseek-chat',
        messages: apiMessages,
        max_tokens: 800,
        temperature: 0.7,
        stream: false
      },
      success: function (res) {
        self._stopTimer()
        if (res.statusCode !== 200) {
          var errText = '服务暂不可用（' + res.statusCode + '），请稍后重试。'
          if (res.statusCode === 402) errText = 'API额度不足，请联系管理员充值。'
          var errMsg = {
            role: 'assistant',
            content: errText,
            loading: false,
            parts: [{ text: errText, isLink: false }],
            relatedLawIds: [],
            relatedCases: []
          }
          var next = self.data.messages.slice(0, -1).concat([errMsg])
          self.setData({ messages: next, sending: false }, function () {
            self._scrollBottom()
            toStorage(next)
          })
          return
        }
        var choice = res.data && res.data.choices && res.data.choices[0]
        var raw = (choice && choice.message && choice.message.content) || ''
        var content = String(raw).trim() || '（无回复内容）'

        var assistantMsg = {
          role: 'assistant',
          content: content,
          loading: false,
          parts: lawLink.parseLawRefs(content),
          relatedLawIds: lawLink.extractLawIds(content),
          relatedCases: []
        }
        var next = self.data.messages.slice(0, -1).concat([assistantMsg])
        self.setData({ messages: next, sending: false }, function () {
          self._scrollBottom()
          toStorage(next)
          setTimeout(function () {
            var cases = app.globalData.cases || []
            var ids = assistantMsg.relatedLawIds
            var relatedCases = findCasesByArticles(cases, ids)
            if (relatedCases.length) {
              var idx = next.length - 1
              var key = 'messages[' + idx + '].relatedCases'
              self.setData({ [key]: relatedCases })
            }
          }, 100)
        })
      },
      fail: function () {
        self._stopTimer()
        var errMsg = {
          role: 'assistant',
          content: '请求失败，请检查网络或稍后再试。',
          loading: false,
          parts: [{ text: '请求失败，请检查网络或稍后再试。', isLink: false }],
          relatedLawIds: [],
          relatedCases: []
        }
        var next = self.data.messages.slice(0, -1).concat([errMsg])
        self.setData({ messages: next, sending: false }, function () {
          self._scrollBottom()
          toStorage(next)
        })
      }
    })
  }
})
