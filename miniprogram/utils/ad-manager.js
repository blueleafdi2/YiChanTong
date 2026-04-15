var CONFIG = {
  enabled: false,
  bannerAdUnitId: '',
  interstitialAdUnitId: '',
  rewardedAdUnitId: '',
  nativeAdUnitId: '',
  bannerPages: ['pages/index/index', 'subpkg/case-detail/case-detail'],
  interstitialFrequency: 5,
  minSessionMinutes: 3
}

var _sessionViews = 0
var _sessionStart = Date.now()

function isReady() {
  if (!CONFIG.enabled) return false
  var elapsed = (Date.now() - _sessionStart) / 60000
  return elapsed >= CONFIG.minSessionMinutes
}

function trackPageView() {
  _sessionViews++
}

function shouldShowInterstitial() {
  if (!isReady() || !CONFIG.interstitialAdUnitId) return false
  return _sessionViews > 0 && _sessionViews % CONFIG.interstitialFrequency === 0
}

function createBannerAd(page) {
  if (!isReady() || !CONFIG.bannerAdUnitId) return null
  if (CONFIG.bannerPages.indexOf(page) === -1) return null
  try {
    var ad = wx.createBannerAd({
      adUnitId: CONFIG.bannerAdUnitId,
      adIntervals: 60,
      style: { left: 0, top: 0, width: 300 }
    })
    return ad
  } catch (e) {
    return null
  }
}

function showInterstitialAd() {
  if (!shouldShowInterstitial()) return
  try {
    var ad = wx.createInterstitialAd({ adUnitId: CONFIG.interstitialAdUnitId })
    ad.show().catch(function () {})
  } catch (e) {}
}

function createRewardedAd(callback) {
  if (!CONFIG.enabled || !CONFIG.rewardedAdUnitId) return null
  try {
    var ad = wx.createRewardedVideoAd({ adUnitId: CONFIG.rewardedAdUnitId })
    ad.onClose(function (res) {
      if (res && res.isEnded) {
        callback && callback(true)
      } else {
        callback && callback(false)
      }
    })
    return ad
  } catch (e) {
    return null
  }
}

function updateConfig(newConfig) {
  if (!newConfig) return
  Object.keys(newConfig).forEach(function (k) {
    if (CONFIG.hasOwnProperty(k)) {
      CONFIG[k] = newConfig[k]
    }
  })
}

module.exports = {
  trackPageView: trackPageView,
  shouldShowInterstitial: shouldShowInterstitial,
  createBannerAd: createBannerAd,
  showInterstitialAd: showInterstitialAd,
  createRewardedAd: createRewardedAd,
  updateConfig: updateConfig,
  isReady: isReady
}
