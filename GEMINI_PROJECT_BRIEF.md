# Void Phone Launcher — Project Brief for Gemini

## Who you are helping
Angus (Cape Town, South Africa). Non-developer building an Android launcher app with AI assistance. Uses Android Studio but is not a Kotlin expert — needs clear, copy-paste-ready code changes with explanations of what to do in Android Studio step by step.

---

## What this project is

**Void Phone** is a minimalist Android home screen launcher.
- Package: `com.clearphone.launcher`
- Language: Kotlin + Jetpack Compose
- IDE: Android Studio
- Project location on Angus's machine: `C:\Users\User\AndroidStudioProjects\SimplePhoneLauncher` — this is the single master: the buildable Android Studio project, the git repo, and the GitHub source all in one. (Edit here, build here, commit here; no separate working-folder copies.)
- GitHub: https://github.com/angusm99/void-phone-launcher

The design aesthetic is: **pure black AMOLED background, transparent circles with lowercase app names inside, no icons by default, optional CRT terminal mode with phosphor effects.**

---

## Current build status (as of April 2026)

The app **builds and runs** on the Android emulator and on a real phone (sideloaded via Bluetooth APK transfer). Core features are all working.

### Source files (3 files total, all in `app/src/main/java/com/clearphone/launcher/`)

| File | Purpose |
|------|---------|
| `MainActivity.kt` | All UI — ~1500 lines, single-file Compose app |
| `FavouritesStore.kt` | DataStore persistence for all settings + enums |
| `AppIconImage.kt` | Renders app Drawable icons with clip shape |

### AndroidManifest extras needed
- `HOME` + `DEFAULT` intent filters (registered as system launcher)
- `<queries>` block for PackageManager visibility on Android 11+
- `launchMode="singleTask"`

### build.gradle.kts dependencies
- `androidx.datastore:datastore-preferences`
- `com.google.accompanist:accompanist-drawablepainter`
- `compileSdk = 36`, `targetSdk = 36`

---

## Architecture — important rules

**DO NOT use Navigation Compose.** Navigation is done with a simple `enum class Screen { Home, AllApps, Help }` and a `when` block. State is held directly in `MainActivity` as `mutableStateOf` properties.

**Single activity, no fragments.** Everything is in `MainActivity.kt`.

**All settings persist via DataStore** through `FavouritesStore`. Every toggle has a corresponding `save` and `load` function.

**Enums are defined in `FavouritesStore.kt`** (not MainActivity):
- `IconShape`: CIRCLE, ROUNDED_SQUARE, DIAMOND
- `ThemeColor`: BLUE, GREEN, PINK, AMBER
- `IconSize`: SMALL (72dp), MEDIUM (104dp), LARGE (148dp)
- `GridColumns`: ONE, TWO, THREE

---

## Current features — what is already built

### Home Screen
- Title: `void phone  v1.0.1` (version pulled from `BuildConfig.VERSION_NAME`)
- Subtitle: `[ build_002 :: essentials_only ]` (normal mode) / `v1.0.1 :: build_002 :: sys_ready` (terminal mode)
- Dynamic grid of circle/rounded-square tiles, each showing app name in lowercase
- Grid layout: 1, 2, or 3 columns — user controlled, centred vertically in available space
- Circle sizes: Small (72dp), Medium (104dp), Large (148dp) — user controlled
- Tile glow: radial gradient bloom behind each circle using `drawWithContent`
- Tile interior: subtle radial gradient (lighter centre → dark edge)
- Long press tile → confirmation dialog to remove from home screen
- "All Apps" pill button at bottom

### Bottom Bar (7 controls + ⋮ menu)
All controls are 40dp circle icon buttons:
- `◯/▢` — shape toggle (circle ↔ rounded square)
- `⊞` — icons toggle (show actual app icons inside circles vs text)
- `S/M/L` — icon size cycle
- `1/2/3` — grid columns cycle
- `>_` — terminal mode toggle
- Colour dot — theme cycle (Blue → Green → Pink → Amber → Blue)
- `⋮` menu: help, set as default launcher, return to previous launcher, close app
- Nav bar inset: `WindowInsets.navigationBars` padding applied so bar never hides behind system buttons

### Colour Themes (4 themes, each with 6 shades)
```
AccentTheme(name, primary, bright, light, dim, faint, dot)
Blue:  primary=#2563EB, bright=#3B82F6, light=#60A5FA
Green: primary=#16A34A, bright=#22C55E, light=#4ADE80
Pink:  primary=#DB2777, bright=#EC4899, light=#F472B6
Amber: primary=#D97706, bright=#F59E0B, light=#FBBF24
```
Background: `#020508` (near black). CardDark: `#0E1520`. TextHint: `#475569`.

### Terminal Mode
Toggled with `>_` button. When active:
- Font switches to JetBrains Mono (falls back to `FontFamily.Monospace` if TTF not installed)
- Title becomes `C:\VOID\> void_phone_` with blinking cursor animation
- Circle labels become `C:\> appname`
- Background: fine horizontal scanline grid (1px black bands every 3px)
- Phosphor tint: theme `accent.primary` at 0.28 alpha overlaid on screen
- Deeper vignette: black 0.65 alpha, radius 0.88× screen width
- **Scanline crackle**: two thin bands at ~22% and ~61% screen height that snap on/off in an 11s keyframe cycle — NOT a smooth scrolling effect
- **Static burst**: crosshatch pattern (horizontal + vertical lines at 2px spacing) fires at sparse keyframe windows in a 13s cycle
- **Flicker**: sparse brightness dips driven by keyframes in 17s cycle via `graphicsLayer { alpha }`
- **Distortion bar**: horizontal glitch band fires once per 15s cycle, jumps 3 vertical positions
- **Radiation warning**: every 5 min — home fades out (2s), ☢ WARNING screen appears (10s linger), home fades back (2s). CRITICAL: the warning overlay is a **sibling Box** outside the home Box, not a child — this is required so the home's `graphicsLayer alpha = 0` doesn't also kill the warning overlay.
- Background grain: 400 white noise dots at low alpha

### Normal Mode (terminal off)
- Font: Inter (falls back to `FontFamily.Default`)
- Background grain: 200 white noise dots at very low alpha (0.025 max) — subtle texture
- Circles: transparent background, theme-coloured border

### Custom Fonts (partially set up)
Code is wired up with `try/catch` fallback. To activate, drop these files into `app/src/main/res/font/`:
- `inter_regular.ttf`, `inter_medium.ttf`, `inter_semibold.ttf` — from fonts.google.com/specimen/Inter (rename from `Inter_18pt-Regular.ttf` etc.)
- `jetbrains_mono_regular.ttf`, `jetbrains_mono_light.ttf` — from fonts.google.com/specimen/JetBrains+Mono

### All Apps Screen
- Full alphabetical list of installed apps
- Each row: icon + app name
- Long press → add to home screen (toast confirmation)
- Already-added apps show ★

### Help Screen
- Explains all controls
- Accessible from ⋮ menu

---

## Known working things — DO NOT change these

- `graphicsLayer { alpha }` on the home Box for radiation warning fade — this fades ALL children including any overlays, so the radiation warning MUST be a sibling, not a child
- `runBlocking` in `onCreate` for initial DataStore load — intentional, not a bug
- `@Suppress("DEPRECATION")` on `queryIntentActivities` — needed for API compatibility
- Bottom bar uses `.align(Alignment.BottomCenter)` — works because it's inside a Box (the inner home Box), which is a Box scope

---

## Versioning / naming convention
- Free tier: **core_v1** — will increment to core_v2, core_v3 with each free update
- Premium tier: **core-titanium** — shown in subtitle when unlocked: `[ core-titanium :: unlocked ]`

---

## Custom Terminal Header (BUILT — was previously premium roadmap)
- Long-press the home title → rename dialog
- Persisted in DataStore as `terminal_header` (default `"void"`)
- Terminal title becomes `C:\{HEADER}\> void_phone_`
- Tile labels in terminal mode: `C:\{HEADER}\> appname`
- Auto-fetch from `android.os.Build.MODEL` is NOT yet wired — text is user-typed only

## Planned premium features (NOT YET BUILT)

### 1. Icon Pack Support (Premium)
- Query installed icon packs via `themeit.app.launcher.THEME` / `org.adw.launcher.THEMES` intent filters
- Parse `appfilter.xml` to map package names → custom drawables
- Designed to work with VOID_UI icon pack (separate APK Angus is building in Illustrator/Figma)
- Stock icons (from `PackageManager.loadIcon()`) are always the fallback — Android has no API to read icons from another launcher's icon pack

### 2. Focus Mode / Work Hours (Major Premium Feature)
Time-based launcher lockdown. Three profiles:

**Work/Company profile:**
- Lock to approved apps between 08:00–17:00
- Company logo + branding shown on restricted screen
- Tea break / lunch windows briefly restore other apps
- Target market: factories, warehouses — stop YouTube/TikTok on work devices while keeping WhatsApp + Google Sheets

**Family/Parent profile:**
- Free-time windows (e.g. 15:00–17:00 weekdays)
- Outside windows: restricted to approved apps
- Child can request extension — parent approves via PIN

**Elderly Care profile:**
- Night/quiet hours: screen shows single giant emergency call button
- Tap calls designated family checkpoint number (WhatsApp preferred)
- Escalation chain if no answer
- Daytime: normal launcher with Large icons + 1 column

**Implementation notes:**
- `AlarmManager` or `WorkManager` for time-based switching
- Per-profile app whitelists stored in DataStore
- Premium unlock via Google Play Billing (one-time IAP)

---

## Play Store readiness — TODO list

The app is ready to submit as **core_v1 (free)**. Pending:
1. App icon — 512×512 PNG, no alpha: circle outline + "void phone" lowercase, black background
2. Feature graphic — 1024×500 PNG banner
3. Screenshots — 4–5 phone screenshots (home screen, terminal mode, all apps, different themes)
4. Short description (80 chars max) + full description text
5. Privacy policy URL (can be hosted free on GitHub Pages — one paragraph)
6. Set `versionCode` and `versionName` in `build.gradle.kts`
7. Build → Generate Signed Bundle / APK (release AAB)
8. Google Play Developer account ($25 USD one-time fee — Angus has this already)
9. Upload AAB, fill metadata, submit for review (3–7 day review time)

---

## How to help Angus

- Always give **complete, copy-paste-ready code blocks** — not partial diffs
- When editing `MainActivity.kt`, show the entire relevant function, not just the changed lines
- After any code change, tell him exactly: (1) which file to update in Android Studio, (2) whether to copy from the external folder or paste directly, (3) to press **Ctrl+F9** to rebuild
- If a change requires a new dependency in `build.gradle.kts`, include the exact line to add
- If a change requires a new import, include it
- Keep explanations short — Angus understands what he wants, just needs the implementation
- The app sideloads to his real phone (Samsung, named "Pickle Rick") via Bluetooth APK transfer for testing

---

## Prompt to start a new session

Paste this at the start of a new Gemini conversation along with the source files:

> I am building an Android launcher app called "Void Phone" in Kotlin + Jetpack Compose. I have attached the current source files: MainActivity.kt, FavouritesStore.kt, and AppIconImage.kt. Please read the GEMINI_PROJECT_BRIEF.md file carefully — it explains the full project, what is already built, the architecture rules, and what I want to build next. Once you have read everything, confirm you understand the project and ask me what I want to work on today.
