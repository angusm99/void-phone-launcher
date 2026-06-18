package com.clearphone.launcher

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("launcher_prefs")

class FavouritesStore(private val context: Context) {
    companion object {
        private val FAVOURITES_KEY     = stringPreferencesKey("favourites")
        private val SHOW_LABELS_KEY    = booleanPreferencesKey("show_labels")
        private val ICON_SHAPE_KEY     = stringPreferencesKey("icon_shape")
        private val THEME_COLOR_KEY    = stringPreferencesKey("theme_color")
        private val TERMINAL_MODE_KEY  = booleanPreferencesKey("terminal_mode")
        private val SHOW_ICONS_KEY     = booleanPreferencesKey("show_icons")
        private val ICON_SIZE_KEY      = stringPreferencesKey("icon_size")
        private val GRID_COLUMNS_KEY   = stringPreferencesKey("grid_columns")
        private val TERMINAL_HEADER_KEY = stringPreferencesKey("terminal_header")
        private val ICON_PACK_KEY      = stringPreferencesKey("icon_pack")
    }

    suspend fun saveFavourites(packageNames: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[FAVOURITES_KEY] = packageNames.joinToString(",")
        }
    }

    suspend fun loadFavourites(): List<String> {
        val prefs = context.dataStore.data.first()
        val saved = prefs[FAVOURITES_KEY] ?: ""
        return if (saved.isEmpty()) emptyList() else saved.split(",")
    }

    suspend fun hasSavedFavourites(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[FAVOURITES_KEY] != null
    }

    suspend fun saveShowLabels(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_LABELS_KEY] = show
        }
    }

    suspend fun loadShowLabels(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[SHOW_LABELS_KEY] ?: true
    }

    suspend fun saveIconShape(shape: IconShape) {
        context.dataStore.edit { prefs ->
            prefs[ICON_SHAPE_KEY] = shape.name
        }
    }

    suspend fun loadIconShape(): IconShape {
        val prefs = context.dataStore.data.first()
        val saved = prefs[ICON_SHAPE_KEY] ?: IconShape.CIRCLE.name
        return IconShape.valueOf(saved)
    }

    suspend fun saveThemeColor(color: ThemeColor) {
        context.dataStore.edit { prefs ->
            prefs[THEME_COLOR_KEY] = color.name
        }
    }

    suspend fun loadThemeColor(): ThemeColor {
        val prefs = context.dataStore.data.first()
        val saved = prefs[THEME_COLOR_KEY] ?: ThemeColor.BLUE.name
        return ThemeColor.valueOf(saved)
    }

    suspend fun saveTerminalMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TERMINAL_MODE_KEY] = enabled
        }
    }

    suspend fun loadTerminalMode(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[TERMINAL_MODE_KEY] ?: false
    }

    suspend fun saveShowIcons(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_ICONS_KEY] = show
        }
    }

    suspend fun loadShowIcons(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[SHOW_ICONS_KEY] ?: false
    }

    suspend fun saveIconSize(size: IconSize) {
        context.dataStore.edit { prefs ->
            prefs[ICON_SIZE_KEY] = size.name
        }
    }

    suspend fun loadIconSize(): IconSize {
        val prefs = context.dataStore.data.first()
        val saved = prefs[ICON_SIZE_KEY] ?: IconSize.MEDIUM.name
        return IconSize.valueOf(saved)
    }

    suspend fun saveGridColumns(cols: GridColumns) {
        context.dataStore.edit { prefs ->
            prefs[GRID_COLUMNS_KEY] = cols.name
        }
    }

    suspend fun loadGridColumns(): GridColumns {
        val prefs = context.dataStore.data.first()
        val saved = prefs[GRID_COLUMNS_KEY] ?: GridColumns.TWO.name
        return GridColumns.valueOf(saved)
    }

    suspend fun saveTerminalHeader(name: String) {
        context.dataStore.edit { prefs ->
            prefs[TERMINAL_HEADER_KEY] = name
        }
    }

    suspend fun loadTerminalHeader(): String {
        val prefs = context.dataStore.data.first()
        return prefs[TERMINAL_HEADER_KEY] ?: "void"
    }

    // Icon pack — empty string means "stock icons" (no pack)
    suspend fun saveIconPack(packageName: String) {
        context.dataStore.edit { prefs ->
            prefs[ICON_PACK_KEY] = packageName
        }
    }

    suspend fun loadIconPack(): String {
        val prefs = context.dataStore.data.first()
        return prefs[ICON_PACK_KEY] ?: ""
    }
}

enum class IconShape {
    CIRCLE,
    ROUNDED_SQUARE,
    DIAMOND
}

enum class ThemeColor {
    BLUE,
    GREEN,
    PINK,
    AMBER
}

// Icon size — controls circle diameter (see iconSizeDp() in MainActivity)
enum class IconSize {
    SMALL,   // 72dp
    MEDIUM,  // 104dp (default)
    LARGE    // 148dp
}

// Grid columns — 1, 2, or 3 columns
enum class GridColumns {
    ONE,
    TWO,
    THREE
}
