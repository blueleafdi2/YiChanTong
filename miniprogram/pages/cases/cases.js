const analytics = require('../../utils/analytics')

function topTags(cases, limit) {
  const freq = {}
  ;(cases || []).forEach((c) => {
    ;(c.tags || []).forEach((t) => {
      if (!t) return
      freq[t] = (freq[t] || 0) + 1
    })
  })
  return Object.keys(freq)
    .sort((a, b) => freq[b] - freq[a])
    .slice(0, limit)
}

function uniqueSorted(values, desc) {
  const arr = [...new Set(values.filter(Boolean))]
  arr.sort((a, b) => (desc ? (a > b ? -1 : a < b ? 1 : 0) : a.localeCompare(b, 'zh-Hans-CN')))
  return arr
}

function yearFromJudgeDate(d) {
  if (!d || typeof d !== 'string') return ''
  const y = d.split('-')[0]
  return /^\d{4}$/.test(y) ? y : ''
}

function briefSummary(text, max) {
  const s = (text || '').replace(/\s+/g, ' ').trim()
  if (s.length <= max) return s
  return s.substring(0, max) + '…'
}

Page({
  data: {
    totalCases: 0,
    tagBar: [],
    typeBar: [],
    yearOptions: ['全部'],
    provinceOptions: ['全部'],
    yearIndex: 0,
    provinceIndex: 0,
    selectedTag: '',
    selectedType: '',
    caseList: [],
    empty: false,
    loaded: false
  },

  _rawCases: [],

  onLoad() {
    const app = getApp()
    this._rawCases = app.globalData.cases || []
    console.log('[Cases] loaded', this._rawCases.length, 'cases')
    this.buildFilterOptions(this._rawCases)
    this.applyFilters()
    this.setData({ loaded: true })
  },

  onShow() {
    analytics.logScreen('cases')
  },

  buildFilterOptions(cases) {
    const tags = topTags(cases, 20)
    const types = uniqueSorted(
      cases.map((c) => c.caseType).filter(Boolean),
      false
    )
    const years = uniqueSorted(
      cases.map((c) => yearFromJudgeDate(c.judgeDate)).filter(Boolean),
      true
    )
    const provinces = uniqueSorted(
      cases.map((c) => c.province).filter(Boolean),
      false
    )
    this.setData({
      totalCases: cases.length,
      tagBar: ['全部'].concat(tags),
      typeBar: ['全部'].concat(types),
      yearOptions: ['全部'].concat(years),
      provinceOptions: ['全部'].concat(provinces)
    })
  },

  applyFilters() {
    const {
      selectedTag,
      selectedType,
      yearIndex,
      provinceIndex,
      yearOptions,
      provinceOptions
    } = this.data
    let list = this._rawCases.slice()

    const yearVal = yearOptions[yearIndex] || '全部'
    const provVal = provinceOptions[provinceIndex] || '全部'

    if (selectedTag) {
      list = list.filter((c) => (c.tags || []).indexOf(selectedTag) !== -1)
    }
    if (selectedType) {
      list = list.filter((c) => c.caseType === selectedType)
    }
    if (yearVal && yearVal !== '全部') {
      list = list.filter((c) => (c.judgeDate || '').indexOf(yearVal) === 0)
    }
    if (provVal && provVal !== '全部') {
      list = list.filter((c) => c.province === provVal)
    }

    list.sort((a, b) => {
      const da = a.judgeDate || ''
      const db = b.judgeDate || ''
      if (da > db) return -1
      if (da < db) return 1
      return 0
    })

    const caseList = list.map((c) =>
      Object.assign({}, c, {
        summaryBrief: briefSummary(c.caseSummary, 50),
        cardTags: (c.tags || []).slice(0, 3)
      })
    )

    this.setData({
      caseList,
      empty: caseList.length === 0
    })
  },

  onTagTap(e) {
    const { tag } = e.currentTarget.dataset
    const selectedTag = tag === '全部' ? '' : tag
    this.setData({ selectedTag })
    this.applyFilters()
  },

  onTypeTap(e) {
    const { type } = e.currentTarget.dataset
    const selectedType = type === '全部' ? '' : type
    this.setData({ selectedType })
    this.applyFilters()
  },

  onYearChange(e) {
    const yearIndex = Number(e.detail.value) || 0
    this.setData({ yearIndex })
    this.applyFilters()
  },

  onProvinceChange(e) {
    const provinceIndex = Number(e.detail.value) || 0
    this.setData({ provinceIndex })
    this.applyFilters()
  },

  onCaseTap(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    wx.navigateTo({ url: `/subpkg/case-detail/case-detail?id=${id}` })
  }
})
