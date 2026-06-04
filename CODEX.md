# VOID UI Phone Launcher — Codex

**Package:** `com.clearphone.launcher`  
**Version:** `1.0.1` (versionCode 2) — single source of truth in `build.gradle.kts`, read via `BuildConfig`  
**Stack:** Kotlin + Jetpack Compose, single activity, no Navigation Compose  
**Build status:** ✅ Debug APK builds clean (Gradle, JBR 21, build-tools 36) — last built 2026-06-04  
**Last updated:** 2026-06-04

---

## Folders on This PC

| What | Path |
|------|------|
| Working folder (source + docs + git) | `C:\Users\User\CLAUDE\VOID_UI_LAUNCHER_MASTER\` |
| Android Studio project | `C:\Users\User\AndroidStudioProjects\SimplePhoneLauncher\` |
| Built debug APK | `C:\Users\User\AndroidStudioProjects\SimplePhoneLauncher\app\build\outputs\apk\debug\app-debug.apk` |
| Obsidian note | `C:\Obsidian Home-MASTER\Obsidian - Home\VOID UI-Phone Launcher App.md` |
| GitHub | https://github.com/angusm99/void-phone-launcher |

---

## Source Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | All UI — HomeScreen, AllAppsScreen, HelpScreen, terminal effects, state |
| `FavouritesStore.kt` | DataStore persistence + all enums |
| `AppIconImage.kt` | Renders app icons with clip shape (incl. DiamondShape) |
| `AndroidManifest.xml` | Launcher registration, `<queries>` block, `singleTask` |
| `build.gradle.kts` | App module — `compileSdk 36`, `minSdk 26` |

---

## Working Folder → Android Studio Sync

Copy these files **into** the Android Studio project before building:

```
VOID_UI_LAUNCHER_MASTER\MainActivity.kt        →  app/src/main/java/com/clearphone/launcher/
VOID_UI_LAUNCHER_MASTER\FavouritesStore.kt     →  app/src/main/java/com/clearphone/launcher/
VOID_UI_LAUNCHER_MASTER\AppIconImage.kt        →  app/src/main/java/com/clearphone/launcher/
VOID_UI_LAUNCHER_MASTER\AndroidManifest.xml    →  app/src/main/
VOID_UI_LAUNCHER_MASTER\build.gradle.kts       →  app/
```

After editing in Android Studio, copy `.kt` files back and commit to GitHub.

---

## Build & Sideload to Pickle Rick

### Option A — Command line (no Android Studio needed)
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
& "C:\Users\User\AndroidStudioProjects\SimplePhoneLauncher\gradlew.bat" -p "C:\Users\User\AndroidStudioProjects\SimplePhoneLauncher" assembleDebug
```
APK lands at `app\build\outputs\apk\debug\app-debug.apk` (~11.6 MB, debug-signed).

### Option B — Android Studio
**Build → Build Bundle(s) / APK(s) → Build APK(s)**

### Install on phone
- **Bluetooth:** send the APK to "Pickle Rick", tap the received file, allow "install unknown apps", install.
- **ADB (if USB-connected):**
  ```
  adb install -r "C:\Users\User\AndroidStudioProjects\SimplePhoneLauncher\app\build\outputs\apk\debug\app-debug.apk"
  ```
- After install: open **void phone**, then set as default via the `⋮` menu → "set as default launcher".

---

## Project State

| Feature | Status |
|---------|--------|
| Home, All Apps, Help screens | ✅ Done |
| Terminal mode (full CRT effects) | ✅ Done |
| Custom terminal header (long-press to rename) | ✅ Done |
| 3 icon shapes: Circle / Rounded Square / Diamond | ✅ Done |
| 4 colour themes × 6 shades | ✅ Done |
| DataStore persistence (9 keys) | ✅ Done |
| App label shows "void phone" | ✅ Fixed (was "Simple Phone Launcher") |
| Version single-sourced via BuildConfig | ✅ Done (`1.0.1`) |
| Debug APK builds + installs | ✅ Verified 2026-06-04 |
| Custom fonts (Inter + JetBrains Mono) | ⛔ Not used — code uses system Monospace/Default. Optional polish only |
| App icon (512×512 PNG) | ⏳ Not designed yet |
| Play Store screenshots | ⏳ Not taken yet |

---

## Resolved in this pass (2026-06-04)

- ✅ **Version string** — no longer hardcoded; `MainActivity` reads `BuildConfig.VERSION_NAME`/`VERSION_CODE`. One source of truth: `build.gradle.kts` (`1.0.1` / code 2).
- ✅ **App label** — `strings.xml app_name` changed from "Simple Phone Launcher" → "void phone".
- ✅ **Stale dp comments** — `FavouritesStore.kt` IconSize comments corrected to 104dp / 148dp.
- ℹ️ **Radiation warning** is on a **5-minute** cycle (2s fade in, 10s hold, 2s fade out) — not 60s as older docs said.
- ℹ️ **Fonts** — current code does NOT load custom TTFs; it uses `FontFamily.Monospace` / `FontFamily.Default`. No missing-resource risk. Custom fonts are optional future polish.

## Before Play Store Submission

1. Design app icon — 512×512 PNG, no alpha, black bg (replace default `ic_launcher`)
2. Design feature graphic — 1024×500 PNG
3. Take 4–5 screenshots (home, terminal mode, all apps, theme variants, 1-column large)
4. Write short description (80 chars) + full description
5. Set up privacy policy URL (GitHub Pages)
6. Build signed release AAB — **Build → Generate Signed Bundle**
7. Upload to Play Console and submit (3–7 day review)

> Note: the GitHub repo / working folder holds the loose source files + docs, not the full Gradle project. The buildable project is the Android Studio one. The `app_name` fix lives in `strings.xml` inside that project (not mirrored to the working folder).

---

## Docs Index

| File | Purpose |
|------|---------|
| `README.md` | Public project overview |
| `CLAUDE.md` | Technical reference (AI context) |
| `GEMINI_PROJECT_BRIEF.md` | Gemini AI brief |
| `PROJECT.md` | Original spec |
| `CODEX.md` | This file |
| Obsidian: `VOID UI-Phone Launcher App.md` | Full verified status + architecture rules |
