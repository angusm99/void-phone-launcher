# VOID UI Phone Launcher — Codex

**Package:** `com.clearphone.launcher`  
**Version:** `1.1.1` (versionCode 4) — single source of truth in `build.gradle.kts`, read via `BuildConfig`  
**Stack:** Kotlin + Jetpack Compose, single activity, no Navigation Compose  
**Build status:** ✅ Debug APK builds clean (Gradle, JBR 21, build-tools 36) — last built 2026-06-04  
**Last updated:** 2026-06-04

---

## Folders on This PC

> **One master.** The Android Studio project **is** the git repo and the GitHub master.
> There are no separate "working folder" copies anymore — edit here, build here, commit here.

| What | Path |
|------|------|
| **Master (project + repo + docs)** | `C:\Users\User\AndroidStudioProjects\SimplePhoneLauncher\` |
| Built debug APK | `…\SimplePhoneLauncher\app\build\outputs\apk\debug\app-debug.apk` |
| Latest test APK (copy) | `C:\Users\User\Desktop\void-phone-v1.0.1-debug.apk` |
| Obsidian note | `C:\Obsidian Home-MASTER\Obsidian - Home\VOID UI-Phone Launcher App.md` |
| GitHub (full buildable project) | https://github.com/angusm99/void-phone-launcher |

Source lives under `app/src/main/java/com/clearphone/launcher/`; resources under `app/src/main/res/`.

---

## Source Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | All UI — HomeScreen, AllAppsScreen, HelpScreen, terminal effects, state |
| `FavouritesStore.kt` | DataStore persistence + all enums |
| `AppIconImage.kt` | Renders app icons with clip shape (incl. DiamondShape) |
| `IconPackManager.kt` | Detects ADW icon packs, parses `appfilter.xml`, resolves app→pack drawable (stock fallback) |
| `AndroidManifest.xml` | Launcher registration, `<queries>` block, `singleTask` |
| `build.gradle.kts` | App module — `compileSdk 36`, `minSdk 26` |

---

## Workflow (single master — no copying between folders)

1. Edit code/docs directly in `AndroidStudioProjects\SimplePhoneLauncher`.
2. Build the APK (see below).
3. Commit + push:
   ```powershell
   cd "C:\Users\User\AndroidStudioProjects\SimplePhoneLauncher"
   git add -A; git commit -m "your message"; git push
   ```

> History note (2026-06-04): previously the repo held only loose source files in a
> separate `VOID_UI_LAUNCHER_MASTER` folder, kept in sync by hand. That caused drift.
> The repo is now the **complete buildable project**; the loose-files copies were deleted.

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
| **Icon pack support (ADW appfilter.xml)** | ✅ Done v1.1.0 — ⋮ menu → "icon pack" |
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
- ✅ **Radiation warning stuck-on bug (v1.1.1)** — on devices with animations disabled (`animator_duration_scale = 0`), Compose `InfiniteTransition` snaps to its `targetValue`, so the warning (target `1f`) showed permanently. Rewritten to a real-time coroutine timer (`LaunchedEffect` + `delay` + `Animatable`) that ignores animation scale. Now correctly fires ~every 5 min for ~14s on all devices. (Other terminal effects also rely on `InfiniteTransition`; on an animations-off device they render static rather than pulsing — re-enable animations on the device for full fidelity. Not a blocker.)
- ℹ️ **Fonts** — current code does NOT load custom TTFs; it uses `FontFamily.Monospace` / `FontFamily.Default`. No missing-resource risk. Custom fonts are optional future polish.

## Before Play Store Submission

1. Design app icon — 512×512 PNG, no alpha, black bg (replace default `ic_launcher`)
2. Design feature graphic — 1024×500 PNG
3. Take 4–5 screenshots (home, terminal mode, all apps, theme variants, 1-column large)
4. Write short description (80 chars) + full description
5. Set up privacy policy URL (GitHub Pages)
6. Build signed release AAB — **Build → Generate Signed Bundle**
7. Upload to Play Console and submit (3–7 day review)

> Note (2026-06-04): the GitHub repo now **is** the full buildable Gradle project — clone it and build directly. The old loose-files `VOID_UI_LAUNCHER_MASTER` copies were retired.

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
