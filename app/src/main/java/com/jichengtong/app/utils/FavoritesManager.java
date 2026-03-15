package com.jichengtong.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;

public class FavoritesManager {
    private static final String PREFS_NAME = "jichengtong_prefs";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_HISTORY = "history";
    private static FavoritesManager instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    private FavoritesManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized FavoritesManager getInstance(Context context) {
        if (instance == null) instance = new FavoritesManager(context);
        return instance;
    }

    // --- Favorites ---
    public void addFavorite(String type, String id, String title) {
        List<Map<String, String>> favorites = getFavorites();
        for (Map<String, String> f : favorites) {
            if (f.get("id").equals(id) && f.get("type").equals(type)) return;
        }
        Map<String, String> item = new HashMap<>();
        item.put("type", type);
        item.put("id", id);
        item.put("title", title);
        item.put("time", String.valueOf(System.currentTimeMillis()));
        favorites.add(0, item);
        saveFavorites(favorites);
    }

    public void removeFavorite(String type, String id) {
        List<Map<String, String>> favorites = getFavorites();
        favorites.removeIf(f -> f.get("id").equals(id) && f.get("type").equals(type));
        saveFavorites(favorites);
    }

    public boolean isFavorite(String type, String id) {
        for (Map<String, String> f : getFavorites()) {
            if (f.get("id").equals(id) && f.get("type").equals(type)) return true;
        }
        return false;
    }

    public List<Map<String, String>> getFavorites() {
        String json = prefs.getString(KEY_FAVORITES, "[]");
        Type t = new TypeToken<List<Map<String, String>>>(){}.getType();
        List<Map<String, String>> list = gson.fromJson(json, t);
        return list != null ? list : new ArrayList<>();
    }

    private void saveFavorites(List<Map<String, String>> favorites) {
        prefs.edit().putString(KEY_FAVORITES, gson.toJson(favorites)).apply();
    }

    // --- Notes ---
    public void saveNote(String type, String id, String title, String noteText) {
        List<Map<String, String>> notes = getNotes();
        notes.removeIf(n -> n.get("id").equals(id) && n.get("type").equals(type));
        Map<String, String> item = new HashMap<>();
        item.put("type", type);
        item.put("id", id);
        item.put("title", title);
        item.put("note", noteText);
        item.put("time", String.valueOf(System.currentTimeMillis()));
        notes.add(0, item);
        prefs.edit().putString(KEY_NOTES, gson.toJson(notes)).apply();
    }

    public String getNote(String type, String id) {
        for (Map<String, String> n : getNotes()) {
            if (n.get("id").equals(id) && n.get("type").equals(type)) return n.get("note");
        }
        return "";
    }

    public List<Map<String, String>> getNotes() {
        String json = prefs.getString(KEY_NOTES, "[]");
        Type t = new TypeToken<List<Map<String, String>>>(){}.getType();
        List<Map<String, String>> list = gson.fromJson(json, t);
        return list != null ? list : new ArrayList<>();
    }

    // --- History ---
    public void addHistory(String type, String id, String title) {
        List<Map<String, String>> history = getHistory();
        history.removeIf(h -> h.get("id").equals(id) && h.get("type").equals(type));
        Map<String, String> item = new HashMap<>();
        item.put("type", type);
        item.put("id", id);
        item.put("title", title);
        item.put("time", String.valueOf(System.currentTimeMillis()));
        history.add(0, item);
        if (history.size() > 100) history = history.subList(0, 100);
        prefs.edit().putString(KEY_HISTORY, gson.toJson(history)).apply();
    }

    public List<Map<String, String>> getHistory() {
        String json = prefs.getString(KEY_HISTORY, "[]");
        Type t = new TypeToken<List<Map<String, String>>>(){}.getType();
        List<Map<String, String>> list = gson.fromJson(json, t);
        return list != null ? list : new ArrayList<>();
    }
}
