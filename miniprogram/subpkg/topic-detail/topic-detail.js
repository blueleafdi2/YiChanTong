const analytics = require('../../utils/analytics')
const favorites = require('../../utils/favorites')
const lawLink = require('../../utils/law-link')

function mapRelatedLaws(arr) {
  return (arr || []).map((text) => ({
    text,
    lawId: lawLink.extractLawIdFromCitation(text)
  }))
}

Page({
  data: {
    mode: '',
    topic: null,
    faq: null,
    contentParts: [],
    relatedLaws: [],
    relatedCases: [],
    favorited: false,
    notFound: false
  },

  _key: { type: '', id: '' },

  onLoad(options) {
    const topicId = options.topic_id || ''
    const faqId = options.faq_id || ''
    const app = getApp()

    if (topicId) {
      const topic = (app.globalData.topics || []).find((t) => t.id === topicId)
      if (!topic) {
        this.setData({ notFound: true })
        wx.setNavigationBarTitle({ title: '未找到' })
        return
      }
      wx.setNavigationBarTitle({ title: topic.title })
      const contentParts = lawLink.parseLawRefs(topic.content || '')
      favorites.addHistory('topic', topicId, topic.title)
      analytics.logContentView('topic', topicId)
      const cases = app.globalData.cases || []
      const relatedCases = (topic.relatedCaseIds || []).map((cid) => {
        const c = cases.find((x) => x.id === cid)
        return { id: cid, title: c ? c.title : cid }
      })
      this._key = { type: 'topic', id: topicId }
      this.setData({
        mode: 'topic',
        topic,
        contentParts,
        relatedLaws: mapRelatedLaws(topic.relatedLaws),
        relatedCases,
        favorited: favorites.isFavorited('topic', topicId)
      })
      return
    }

    if (faqId) {
      const faq = (app.globalData.faq || []).find((f) => f.id === faqId)
      if (!faq) {
        this.setData({ notFound: true })
        wx.setNavigationBarTitle({ title: '未找到' })
        return
      }
      wx.setNavigationBarTitle({ title: '常见问题' })
      const contentParts = lawLink.parseLawRefs(faq.answer || '')
      favorites.addHistory('faq', faqId, faq.question)
      analytics.logContentView('faq', faqId)
      this._key = { type: 'faq', id: faqId }
      this.setData({
        mode: 'faq',
        faq,
        contentParts,
        relatedLaws: mapRelatedLaws(faq.relatedLaws),
        favorited: favorites.isFavorited('faq', faqId)
      })
      return
    }

    this.setData({ notFound: true })
    wx.setNavigationBarTitle({ title: '未找到' })
  },

  onShow() {
    analytics.logScreen('topic_detail')
    const { type, id } = this._key
    if (type && id) {
      this.setData({ favorited: favorites.isFavorited(type, id) })
    }
  },

  onToggleFavorite() {
    const { mode, topic, faq } = this.data
    if (mode === 'topic' && topic) {
      if (this.data.favorited) {
        favorites.removeFavorite('topic', topic.id)
        this.setData({ favorited: false })
      } else {
        favorites.addFavorite('topic', topic.id, topic.title)
        this.setData({ favorited: true })
        analytics.logFavorite('topic', topic.id)
      }
    } else if (mode === 'faq' && faq) {
      if (this.data.favorited) {
        favorites.removeFavorite('faq', faq.id)
        this.setData({ favorited: false })
      } else {
        favorites.addFavorite('faq', faq.id, faq.question)
        this.setData({ favorited: true })
        analytics.logFavorite('faq', faq.id)
      }
    }
  },

  onLawRefTap(e) {
    const { lawid } = e.currentTarget.dataset
    if (lawid) lawLink.navigateToLaw(lawid)
  },

  onRelatedLawTap(e) {
    const { lawid } = e.currentTarget.dataset
    if (lawid) lawLink.navigateToLaw(lawid)
  },

  onCaseTap(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    wx.navigateTo({ url: `/subpkg/case-detail/case-detail?id=${id}` })
  }
})
