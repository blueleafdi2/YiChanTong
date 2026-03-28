const KEY = 'yct_analytics'

function getStore() {
  try {
    return JSON.parse(wx.getStorageSync(KEY) || '{}')
  } catch (e) {
    return {}
  }
}

function save(store) {
  try {
    wx.setStorageSync(KEY, JSON.stringify(store))
  } catch (e) {}
}

function log(event, params) {
  const store = getStore()
  if (!store.counts) store.counts = {}
  store.counts[event] = (store.counts[event] || 0) + 1

  if (!store.log) store.log = []
  const entry = { t: new Date().toISOString().replace('T', ' ').substring(0, 19), e: event }
  if (params) entry.p = params
  store.log.push(entry)
  if (store.log.length > 300) store.log = store.log.slice(-300)

  save(store)

  if (typeof wx.reportEvent === 'function') {
    try { wx.reportEvent(event, params || {}) } catch (e) {}
  }
}

function logScreen(name) { log('screen_view', { screen: name }) }
function logSearch(query, count) { log('search', { query, results: count }) }
function logContentView(type, id) { log('content_view', { type, id }) }
function logAI(query) { log('ai_query', { query: query.substring(0, 80) }) }
function logFavorite(type, id) { log('favorite', { type, id }) }

function getStats() {
  const store = getStore()
  return store.counts || {}
}

function getLog() {
  const store = getStore()
  return store.log || []
}

function exportReport() {
  const store = getStore()
  const info = wx.getSystemInfoSync()
  return {
    export_time: new Date().toISOString(),
    device: info.model,
    platform: info.platform,
    wx_version: info.version,
    sdk: info.SDKVersion,
    counts: store.counts || {},
    recent_events: (store.log || []).slice(-50)
  }
}

module.exports = { log, logScreen, logSearch, logContentView, logAI, logFavorite, getStats, getLog, exportReport }
