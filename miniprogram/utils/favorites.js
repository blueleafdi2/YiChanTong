const KEYS = {
  favorites: 'yct_favorites',
  notes: 'yct_notes',
  history: 'yct_history'
}

function getList(key) {
  try { return JSON.parse(wx.getStorageSync(key) || '[]') }
  catch (e) { return [] }
}

function saveList(key, list) {
  try { wx.setStorageSync(key, JSON.stringify(list)) } catch (e) {}
}

function addFavorite(type, id, title) {
  const list = getList(KEYS.favorites)
  if (list.some(i => i.type === type && i.id === id)) return false
  list.unshift({ type, id, title, time: Date.now() })
  saveList(KEYS.favorites, list)
  return true
}

function removeFavorite(type, id) {
  let list = getList(KEYS.favorites)
  list = list.filter(i => !(i.type === type && i.id === id))
  saveList(KEYS.favorites, list)
}

function isFavorited(type, id) {
  return getList(KEYS.favorites).some(i => i.type === type && i.id === id)
}

function getFavorites() { return getList(KEYS.favorites) }

function saveNote(type, id, title, content) {
  const list = getList(KEYS.notes)
  const idx = list.findIndex(i => i.type === type && i.id === id)
  const item = { type, id, title, content, time: Date.now() }
  if (idx >= 0) list[idx] = item
  else list.unshift(item)
  saveList(KEYS.notes, list)
}

function getNote(type, id) {
  return getList(KEYS.notes).find(i => i.type === type && i.id === id)
}

function getNotes() { return getList(KEYS.notes) }

function removeNote(type, id) {
  let list = getList(KEYS.notes)
  list = list.filter(i => !(i.type === type && i.id === id))
  saveList(KEYS.notes, list)
}

function addHistory(type, id, title) {
  let list = getList(KEYS.history)
  list = list.filter(i => !(i.type === type && i.id === id))
  list.unshift({ type, id, title, time: Date.now() })
  if (list.length > 100) list = list.slice(0, 100)
  saveList(KEYS.history, list)
}

function getHistory() { return getList(KEYS.history) }

function clearHistory() { saveList(KEYS.history, []) }

module.exports = {
  addFavorite, removeFavorite, isFavorited, getFavorites,
  saveNote, getNote, getNotes, removeNote,
  addHistory, getHistory, clearHistory
}
