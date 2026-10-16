# VOID UI Phone Launcher — Codex

**Package:** `com.clearphone.launcher`  
**Version:** `1.1.4` (versionCode 7) — single source of truth in `build.gradle.kts`, read via `BuildConfig`  
**Stack:** Kotlin + Jetpack Compose, single activity, no Navigation Compose  
**Build status:** ✅ Debug APK and release AAB build clean (Gradle, JBR 21, build-tools 36) — 2026-10-16. Release signing is wired; it fails at signing until the two passwords are filled in `keystore.properties`  
**Last updated:** 2026-10-16

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
| DataStore persistence (10 keys, incl. `icon_pack`) | ✅ Done |
| App label shows "void phone" | ✅ Fixed (was "Simple Phone Launcher") |
| Version single-sourced via BuildConfig | ✅ Done |
| Debug APK builds + installs | ✅ Verified 2026-06-04 |
| Custom fonts (Inter + JetBrains Mono) | ⛔ Not used — code uses system Monospace/Default. Optional polish only |
| App icon (adaptive vector: ring + `>_` on #020508) | ✅ Done v1.1.3 — template robot bitmaps removed |
| Play hi-res icon 512×512 | ✅ `play-store/icon-512.png` (`scripts/make_store_icon.py`) |
| Launch splash / window background | ✅ #020508 (no more white flash) |
| Release signing | ⚙️ Wired via gitignored `keystore.properties` — **fill in the two passwords** |
| Listing copy + Data safety answers | ✅ `docs/PLAY_STORE_LISTING.md` (all fields within Play limits) |
| Privacy policy | ✅ Live: https://angusm99.github.io/void-phone-launcher/privacy-policy.html |
| Play Store screenshots | ✅ 4 × 1080×1920 in `play-store/` (home, terminal mode, icon pack, all apps) |
| Feature graphic 1024×500 | ✅ `play-store/feature-1024x500.png` (`scripts/make_feature_graphic.py`) |
| Back gesture on All Apps / Help | ✅ Fixed v1.1.4 — returns home instead of closing the launcher |
| ⋮ "return to previous launcher" | ✅ Fixed v1.1.4 — opens another home app explicitly (skips boot FallbackHome) |

---

## Resolved in this pass (2026-06-04)

- ✅ **Version string** — no longer hardcoded; `MainActivity` reads `BuildConfig.VERSION_NAME`/`VERSION_CODE`. One source of truth: `build.gradle.kts` (`1.0.1` / code 2).
- ✅ **App label** — `strings.xml app_name` changed from "Simple Phone Launcher" → "void phone".
- ✅ **Stale dp comments** — `FavouritesStore.kt` IconSize comments corrected to 104dp / 148dp.
- ✅ **Radiation warning (v1.1.1 stuck-on fix → v1.1.2 brief flash)** — on devices with animations disabled (`animator_duration_scale = 0`), Compose `InfiniteTransition` snaps to its `targetValue`, so the warning (target `1f`) showed permanently. Rewritten to a real-time coroutine timer (`LaunchedEffect` + `delay` + `Animatable`) that ignores animation scale, **only runs in terminal mode**, and shows a **brief ~2s flash** every ~5 min (was 14s). (Other terminal effects also rely on `InfiniteTransition`; on an animations-off device they render static rather than pulsing — re-enable animations on the device for full fidelity. Not a blocker.)
- ℹ️ **Fonts** — current code does NOT load custom TTFs; it uses `FontFamily.Monospace` / `FontFamily.Default`. No missing-resource risk. Custom fonts are optional future polish.

## Before Play Store Submission

1. ~~App icon~~ ✅ · ~~512 store icon~~ ✅ · ~~feature graphic~~ ✅ · ~~4 screenshots~~ ✅ · ~~listing copy~~ ✅ · ~~privacy policy live~~ ✅ · ~~signing config~~ ✅
2. Fill in `storePassword` and `keyPassword` in `keystore.properties` (gitignored; template: `keystore.properties.example`)
3. Build the signed bundle: `gradlew bundleRelease` (or Android Studio **Build → Build Bundle(s) / APK(s) → Build Bundle(s)**) → `app/build/outputs/bundle/release/app-release.aab`
4. Play Console: create app → upload to **Closed testing** → 12 testers for 14 days (personal accounts) → then production

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
| `docs/PLAY_STORE_LISTING.md` | Play listing copy, console settings, Data safety answers |
| `docs/privacy-policy.html` | Privacy policy (served by GitHub Pages from `/docs`) |
| `keystore.properties.example` | Release-signing template (real file is gitignored) |
| Obsidian: `Projects/Void Phone.md` | Canonical project note (navigation + handoff) |
