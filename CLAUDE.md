# Void Phone — Android Launcher

## What This Is

A minimalist Android launcher built in Kotlin + Jetpack Compose. Single-activity, no Navigation Compose — screen state is hoisted at the root and switched via a `when` block. Package: `com.clearphone.launcher`.

## Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Everything — Activity, all screens, composables, theme definitions |
| `FavouritesStore.kt` | DataStore persistence layer + enums (`IconShape`, `ThemeColor`, `IconSize`, `GridColumns`) |
| `AppIconImage.kt` | Small composable that renders a `Drawable` as a clipped icon |

## Architecture

```
MainActivity
├── State (mutableStateOf): currentScreen, showLabels, iconShape, themeColor,
│                           terminalMode, showIcons, iconSize, gridColumns
├── favourites: mutableStateListOf<AppItem>  (persisted via FavouritesStore)
├── installedApps: List<AppItem>             (queried on onCreate via PackageManager)
│
├── HomeScreen        — favourites grid, bottom bar controls, terminal effects
├── AllAppsScreen     — full app list (LazyColumn), long-press to add favourite
└── HelpScreen        — usage guide
```

Navigation is purely state-based — no back stack, no NavController.

## Theming

Four accent themes defined at the top of `MainActivity.kt`:

| Name | Primary |
|------|---------|
| Blue | `#2563EB` |
| Green | `#16A34A` |
| Pink | `#DB2777` |
| Amber | `#D97706` |

Each theme has: `primary`, `bright`, `light`, `dim`, `faint`, `dot`. Background is always near-black `#020508`.

Fonts: **Inter** (normal mode) and **JetBrains Mono** (terminal mode). Font files go in `app/src/main/res/font/` — gracefully falls back to system defaults if missing.

## Terminal Mode

When enabled, `HomeScreen` draws a CRT-style overlay via `drawWithContent`:
- Horizontal scanline grid
- Phosphor tint (theme-colored)
- Static burst (crosshatch + pepper dots, keyframe-animated)
- Distortion bar (horizontal glitch band)
- Scanline crackle (two brief flicker bands)
- Screen vignette
- Radiation warning interrupt (fires ~every 5 min, fades the screen and shows ☢ WARNING)
- Title changes to `C:\VOID\> void_phone_` with blinking cursor

All effects use `rememberInfiniteTransition` with keyframe animations. Effects are zero when `terminalMode = false`.

## Persistence (FavouritesStore)

DataStore preferences, all async. Keys:

| Key | Type | Default |
|-----|------|---------|
| `favourites` | String (comma-separated package names) | 6 default apps |
| `show_labels` | Boolean | `true` |
| `icon_shape` | String (enum name) | `CIRCLE` |
| `theme_color` | String (enum name) | `BLUE` |
| `terminal_mode` | Boolean | `false` |
| `show_icons` | Boolean | `false` |
| `icon_size` | String (enum name) | `MEDIUM` |
| `grid_columns` | String (enum name) | `TWO` |
| `terminal_header` | String | `"void"` |

On first launch, 6 default packages are added (dialer, messages, WhatsApp, camera, photos, settings) and saved.

## Enums

```kotlin
enum class IconShape   { CIRCLE, ROUNDED_SQUARE, DIAMOND }
enum class ThemeColor  { BLUE, GREEN, PINK, AMBER }
enum class IconSize    { SMALL, MEDIUM, LARGE }     // 72dp / 104dp / 148dp
enum class GridColumns { ONE, TWO, THREE }
```

`DiamondShape` is defined in `AppIconImage.kt` as a `GenericShape` (4-point diamond from top-centre).

## Bottom Bar Controls

Seven controls in a row at the bottom of HomeScreen:

| Button | Action |
|--------|--------|
| ◯ / ▢ / ◇ | Cycle icon shape (circle → rounded square → diamond) |
| ⊞ | Toggle show icons (vs text labels inside circles) |
| S/M/L | Cycle icon size |
| 1/2/3 | Cycle grid columns |
| >_ | Toggle terminal mode |
| Colour dot | Cycle theme |
| ⋮ | Dropdown: Help / Set as default / Return to system launcher / Close app |

## Key Behaviours

- **Long press** on the home title → opens rename dialog for the terminal header (persists, default `"void"`)
- **Long press** on a home icon → confirmation dialog to remove from favourites
- **Long press** on an All Apps row → adds to favourites (toast feedback); already-added shows "already on home"
- **Home icons**: text label inside circle by default; when `showIcons = true`, renders the actual app icon with name below
- **Terminal mode**: title becomes `C:\{HEADER}\> void_phone_`, tile labels become `C:\{HEADER}\> appname`

## Launcher Registration

Registered in `AndroidManifest.xml` with:
```xml
<action android:name="android.intent.action.MAIN" />
<category android:name="android.intent.category.HOME" />
<category android:name="android.intent.category.DEFAULT" />
```

"Set as default launcher" opens `Settings.ACTION_HOME_SETTINGS`.

## Development Notes

- Avoid Navigation Compose — current state-based approach is intentional and simple
- All persistence calls use `Dispatchers.IO` via `CoroutineScope`; `onCreate` uses `runBlocking` to load prefs before first render
- `getInstalledApps()` filters out the launcher's own package
- Terminal effects are pure `Canvas` draw calls — no third-party libraries
