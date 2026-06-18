# void phone

> A minimalist Android launcher. Transparent circles. Lowercase names. Pure black. Optional CRT terminal mode.

---

## Overview

**void phone** is a distraction-free Android home screen launcher built in Kotlin + Jetpack Compose. No app drawer clutter, no widgets, no icon badges. Just the apps you choose, in circles, on black.

- **Package:** `com.clearphone.launcher`
- **Version:** core_v1 (free tier)
- **Platform:** Android, Jetpack Compose, single-activity architecture
- **Project path (single master = repo):** `C:\Users\User\AndroidStudioProjects\SimplePhoneLauncher`
- **GitHub:** https://github.com/angusm99/void-phone-launcher (full buildable project)

---

## Source Files

| File | Description |
|------|-------------|
| `MainActivity.kt` | All UI — HomeScreen, AllAppsScreen, HelpScreen, tile composables, terminal effects (~1500 lines) |
| `FavouritesStore.kt` | DataStore persistence for all settings + all enums |
| `AppIconImage.kt` | Renders app Drawable icons with clip shape |

---

## Architecture

- **Navigation:** Simple `enum class Screen { Home, AllApps, Help }` with a `when` block — no Navigation Compose
- **State:** All settings held as `mutableStateOf` in `MainActivity`, persisted via DataStore
- **Enums** (defined in `FavouritesStore.kt`):
  - `IconShape` — CIRCLE, ROUNDED_SQUARE, DIAMOND
  - `ThemeColor` — BLUE, GREEN, PINK, AMBER
  - `IconSize` — SMALL (72dp), MEDIUM (104dp), LARGE (148dp)
  - `GridColumns` — ONE, TWO, THREE

---

## Features

### Home Screen
- Title `void phone v1.0.1` (version from `BuildConfig`) / subtitle `[ build_002 :: essentials_only ]`
- Dynamic grid: 1, 2, or 3 columns, centred vertically in available space
- Circle/rounded-square tiles with lowercase app names
- Circle sizes: Small, Medium, Large — user controlled
- Circle glow: radial gradient bloom behind each tile
- Circle interior: subtle radial gradient (lit centre → dark edge)
- Long press → confirmation dialog to remove from home
- All Apps pill button

### Bottom Bar
| Button | Action |
|--------|--------|
| `◯ / ▢ / ◇` | Cycle shape: circle → rounded square → diamond |
| `⊞` | Toggle stock app icons inside circles |
| `S / M / L` | Cycle icon size |
| `1 / 2 / 3` | Cycle grid columns |
| `>_` | Toggle terminal mode |
| Colour dot | Cycle colour theme |
| `⋮` | Menu: help, set default launcher, return to previous launcher, close |

### Custom Terminal Header
- Long-press the home screen title to open a rename dialog
- Text persists via DataStore (`terminal_header` key, default `"void"`)
- Terminal mode title becomes `C:\{HEADER}\> void_phone_` (with blinking cursor)
- Tile labels in terminal mode also use it: `C:\{HEADER}\> appname`
- Radiation warning screen uses it: `C:\{HEADER}\> sys_alert :: code_red`

Nav bar inset applied via `WindowInsets.navigationBars` — bar never hides behind system buttons.

### Colour Themes
Four themes, each with 6 shades (primary, bright, light, dim, faint, dot):

| Theme | Primary | Bright | Light |
|-------|---------|--------|-------|
| Blue | `#2563EB` | `#3B82F6` | `#60A5FA` |
| Green | `#16A34A` | `#22C55E` | `#4ADE80` |
| Pink | `#DB2777` | `#EC4899` | `#F472B6` |
| Amber | `#D97706` | `#F59E0B` | `#FBBF24` |

Background: `#020508` — CardDark: `#0E1520` — TextHint: `#475569`

### Terminal Mode (`>_`)
A CRT phosphor screen aesthetic layered entirely via `drawWithContent` and `graphicsLayer`. All effects are keyframe-driven — no smooth tweens.

| Effect | Detail |
|--------|--------|
| Font | JetBrains Mono (falls back to system monospace) |
| Title | `C:\VOID\> void_phone_` with blinking cursor |
| Scanline grid | 1px black bands every 3px — phosphor row texture |
| Scanline crackle | Two thin bands at fixed ~22% / ~61% height that snap on/off in 11s keyframe cycle |
| Phosphor tint | `accent.primary` at 0.28 alpha over full screen |
| Flicker | `graphicsLayer { alpha }` dips ~12%, sparse spikes in 17s keyframe cycle |
| Static burst | Crosshatch lines + pepper dots, fires at sparse windows in 13s keyframe cycle |
| Distortion bar | Horizontal glitch band, fires once per 15s cycle, jumps 3 positions |
| Vignette | Black radial gradient, 0.65 alpha, 0.88× screen width radius |
| Radiation warning | Every 5 min: home fades out (2s) → ☢ WARNING lingers (10s) → home fades back (2s) |

> **Critical architecture note:** The radiation warning overlay is a **sibling Box** outside the home Box, not a child. This is required because `graphicsLayer { alpha = 0 }` on the home Box fades all of its children — if the warning were a child, it would disappear too.

### Normal Mode Visual Polish
- Font: Inter (falls back to system default)
- Background grain: 200 fine white noise dots at very low alpha — premium dark material feel
- Circle glow and interior radial gradient active in both modes

---

## Custom Fonts (pending activation)

Font code is wired up with `try/catch` fallback — builds cleanly without the files.

To activate, create `app/src/main/res/font/` in Android Studio and add these 5 files (rename from the Google Fonts download):

| File needed | Source name |
|------------|-------------|
| `inter_regular.ttf` | `Inter_18pt-Regular.ttf` |
| `inter_medium.ttf` | `Inter_18pt-Medium.ttf` |
| `inter_semibold.ttf` | `Inter_18pt-SemiBold.ttf` |
| `jetbrains_mono_regular.ttf` | `JetBrainsMono-Regular.ttf` |
| `jetbrains_mono_light.ttf` | `JetBrainsMono-Light.ttf` |

Download from [fonts.google.com/specimen/Inter](https://fonts.google.com/specimen/Inter) and [fonts.google.com/specimen/JetBrains+Mono](https://fonts.google.com/specimen/JetBrains+Mono). **Do not copy the entire font family** — only these 5 files.

---

## Versioning / Naming

- **Free tier:** `core_v1` → increments to `core_v2`, `core_v3` with each free update
- **Premium tier:** `core-titanium` → shown as `[ core-titanium :: unlocked ]` in subtitle

---

## Icon Pack Support — BUILT (v1.1.0)

Standard ADW-style icon packs are supported (see `IconPackManager.kt`):
- Detects installed packs via the `org.adw.launcher.THEMES` / GO / Nova theme intents
- Parses the pack's `assets/appfilter.xml` (or `res/xml/appfilter`) → maps each app's launcher component to a pack drawable
- Exact component match first, then package-name fallback, then the app's stock icon
- Pick a pack from the `⋮` menu → **icon pack**; choice persists (`icon_pack` DataStore key)
- Selecting a pack auto-enables icon display; "stock icons" reverts
- Natural fit for the VOID UI icon pack (`com.voidui.iconpack`)

## Premium Roadmap

### Focus Mode / Work Hours *(priority feature)*
Time-based launcher lockdown. Three profiles:

**Work / Company**
- Lock to approved apps 08:00–17:00
- Branded screen with company logo during restricted hours
- Tea break / lunch windows briefly restore other apps
- Target: factory floors, warehouses — stops YouTube/TikTok while keeping WhatsApp + Sheets

**Family / Parent**
- Define free-time windows (e.g. 15:00–17:00 weekdays)
- Child can request an extension — parent approves via PIN

**Elderly Care**
- Night/quiet hours: single large emergency call button fills screen
- Tap → calls designated family checkpoint (WhatsApp preferred)
- Escalation chain if no answer
- Daytime: normal launcher with Large icons + 1 column

Implementation: `AlarmManager` or `WorkManager`, per-profile app whitelists in DataStore, Google Play Billing one-time IAP unlock.

---

## Resolved (2026-06-04)

- ✅ **Version string** — now single-sourced. `MainActivity` reads `BuildConfig.VERSION_NAME` / `VERSION_CODE`; the only place to set it is `build.gradle.kts` (`1.0.1` / code 2).
- ✅ **App label** — `strings.xml app_name` is now **"void phone"** (was "Simple Phone Launcher").
- ✅ **Stale dp comments** in `FavouritesStore.kt` corrected to `104dp / 148dp`.
- ✅ **Debug APK** builds clean and was verified on device-ready hardware (`com.clearphone.launcher` v1.0.1, ~11.6 MB).

> Note: custom fonts are **not** loaded by the current code (it uses system `Monospace`/`Default`), so there is no missing-font risk. Adding TTFs to `res/font/` is optional polish.

## Play Store Checklist

- [ ] App icon — 512×512 PNG, no alpha (circle outline + "void phone" lowercase, black bg)
- [ ] Feature graphic — 1024×500 PNG banner
- [ ] Screenshots — 4–5 (home, terminal mode, all apps, theme variants, 1-column large)
- [ ] Short description — 80 chars max
- [ ] Full description
- [ ] Privacy policy URL (GitHub Pages, one paragraph)
- [ ] `versionCode` + `versionName` set in `build.gradle.kts`
- [ ] Generate signed release AAB (Build → Generate Signed Bundle)
- [ ] Play Developer account ($25 one-time — already set up)
- [ ] Upload AAB, fill metadata, submit for review (3–7 day review)

---

## Dependencies

```kotlin
// build.gradle.kts
androidx.datastore:datastore-preferences
com.google.accompanist:accompanist-drawablepainter
compileSdk = 36
targetSdk = 36
```

## AndroidManifest requirements

```xml
<!-- Launcher registration -->
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>

<!-- PackageManager visibility (Android 11+) -->
<queries>
    <intent>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent>
</queries>
```

`launchMode="singleTask"` on MainActivity.

---

*void phone — essentials only.*
