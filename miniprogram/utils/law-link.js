var CN_NUM_MAP = {
  '一千一百一十九': '1119', '一千一百二十': '1120', '一千一百二十一': '1121',
  '一千一百二十二': '1122', '一千一百二十三': '1123', '一千一百二十四': '1124',
  '一千一百二十五': '1125', '一千一百二十六': '1126', '一千一百二十七': '1127',
  '一千一百二十八': '1128', '一千一百二十九': '1129', '一千一百三十': '1130',
  '一千一百三十一': '1131', '一千一百三十二': '1132', '一千一百三十三': '1133',
  '一千一百三十四': '1134', '一千一百三十五': '1135', '一千一百三十六': '1136',
  '一千一百三十七': '1137', '一千一百三十八': '1138', '一千一百三十九': '1139',
  '一千一百四十': '1140', '一千一百四十一': '1141', '一千一百四十二': '1142',
  '一千一百四十三': '1143', '一千一百四十四': '1144', '一千一百四十五': '1145',
  '一千一百四十六': '1146', '一千一百四十七': '1147', '一千一百四十八': '1148',
  '一千一百四十九': '1149', '一千一百五十': '1150', '一千一百五十一': '1151',
  '一千一百五十二': '1152', '一千一百五十三': '1153', '一千一百五十四': '1154',
  '一千一百五十五': '1155', '一千一百五十六': '1156', '一千一百五十七': '1157',
  '一千一百五十八': '1158', '一千一百五十九': '1159', '一千一百六十': '1160',
  '一千一百六十一': '1161', '一千一百六十二': '1162', '一千一百六十三': '1163'
}

function isInLibrary(id) {
  var n = parseInt(id, 10)
  return n >= 1119 && n <= 1163
}

function extractLawIds(text) {
  if (!text) return []
  var ids = []
  var arabicReg = /第(\d{3,4})条/g
  var m
  while ((m = arabicReg.exec(text)) !== null) {
    if (isInLibrary(m[1])) ids.push(m[1])
  }
  for (var cn in CN_NUM_MAP) {
    if (text.indexOf('第' + cn + '条') !== -1) ids.push(CN_NUM_MAP[cn])
  }
  var unique = []
  var seen = {}
  ids.forEach(function (id) {
    if (!seen[id]) { seen[id] = true; unique.push(id) }
  })
  return unique
}

function parseLawRefs(text) {
  if (!text) return [{ text: text, isLink: false }]
  var parts = []
  var reg = /第(\d{3,4})条/g
  var last = 0
  var m
  while ((m = reg.exec(text)) !== null) {
    var id = m[1]
    if (isInLibrary(id)) {
      if (m.index > last) parts.push({ text: text.substring(last, m.index), isLink: false })
      var app = getApp()
      var law = app ? app.getLawById(id) : null
      var kw = law && law.keywords ? law.keywords[0] : ''
      parts.push({ text: '第' + id + '条' + (kw ? ' ' + kw : ''), isLink: true, lawId: id })
      last = m.index + m[0].length
    }
  }
  if (last < text.length) parts.push({ text: text.substring(last), isLink: false })
  return parts.length ? parts : [{ text: text, isLink: false }]
}

function navigateToLaw(id) {
  if (!isInLibrary(id)) {
    wx.showToast({ title: '第' + id + '条不在继承编范围', icon: 'none' })
    return
  }
  wx.navigateTo({ url: '/subpkg/law-detail/law-detail?id=' + id })
}

function extractLawIdFromCitation(str) {
  if (!str || typeof str !== 'string') return ''
  var m = str.match(/第\s*(\d{1,4})\s*条/)
  if (!m) return ''
  return isInLibrary(m[1]) ? String(m[1]) : ''
}

module.exports = {
  extractLawIds: extractLawIds,
  parseLawRefs: parseLawRefs,
  navigateToLaw: navigateToLaw,
  CN_NUM_MAP: CN_NUM_MAP,
  extractLawIdFromCitation: extractLawIdFromCitation,
  isInLibrary: isInLibrary
}
