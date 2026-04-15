var REMOTE_URL = 'https://raw.githubusercontent.com/blueleafdi2/YiChanTong/main/remote_config.json'
var CACHE_KEY = 'yct_remote_config'
var CACHE_TTL = 3600000

var _config = null

function getDefault() {
  return {
    version: '2.0.0',
    minVersion: '1.0.0',
    forceUpdate: false,
    updateMessage: '',
    adEnabled: false,
    adConfig: {},
    announcement: '',
    announcementType: 'info',
    features: {
      ai: true,
      lawyer: true,
      share: true,
      export: true
    }
  }
}

function load(callback) {
  try {
    var cached = wx.getStorageSync(CACHE_KEY)
    if (cached) {
      var parsed = JSON.parse(cached)
      if (parsed._ts && Date.now() - parsed._ts < CACHE_TTL) {
        _config = parsed
        callback && callback(_config)
        fetchRemote()
        return
      }
    }
  } catch (e) {}

  _config = getDefault()
  callback && callback(_config)
  fetchRemote()
}

function fetchRemote() {
  wx.request({
    url: REMOTE_URL + '?t=' + Date.now(),
    timeout: 8000,
    success: function (res) {
      if (res.statusCode === 200 && res.data) {
        _config = res.data
        _config._ts = Date.now()
        try {
          wx.setStorageSync(CACHE_KEY, JSON.stringify(_config))
        } catch (e) {}
      }
    },
    fail: function () {}
  })
}

function get(key, defaultValue) {
  if (!_config) return defaultValue
  return _config[key] !== undefined ? _config[key] : defaultValue
}

function getAll() {
  return _config || getDefault()
}

module.exports = {
  load: load,
  get: get,
  getAll: getAll
}
