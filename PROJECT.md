# Simple Phone Launcher – Project Documentation

## Overview
**Simple Phone** is a minimalist Android launcher designed for simplicity, clarity, and usability—especially for older users or those who prefer a distraction-free interface.

**Goal:** Large buttons, minimal UI, predictable behavior, zero clutter.

---

## Current State (Working Features)

### Core App
- **Language:** Kotlin
- **IDE:** Android Studio
- **UI Framework:** Jetpack Compose
- **Persistence:** DataStore (for favourites)
- **Package Query:** PackageManager
- **Status:** Builds, installs, and runs on emulator ✅

### Launcher Registration
- Registered as a system launcher via intent filters:
  - `MAIN`
  - `HOME`
  - `DEFAULT`
- Can act as default home screen (for testing)

### Home Screen
- **Title:** "Simple Phone"
- **Features:**
  - Large buttons (high readability)
  - High contrast design
  - Displays favourite apps dynamically
  - Long press on favourite → removes it + shows toast confirmation
- **Status:** Working ✅

### All Apps Screen
- **Features:**
  - Lists all launchable apps
  - Sorted alphabetically
  - Implemented with LazyColumn (efficient scrolling)
  - Filters out ClearPhone launcher itself
  - App launching works correctly
- **Status:** Working ✅

### Favourites System
- **Add:** Via button or interaction on home screen
- **Remove:** Long press on a favourite app
- **Persistence:** DataStore (survives app restarts) ✅
- **Status:** Working ✅

---

## Navigation Architecture

### Pattern (No Navigation Compose)
Simple state-based navigation:

```kotlin
var currentScreen by mutableStateOf(Screen.Home)

when (currentScreen) {
    Screen.Home -> HomeScreen(
        onOpenAllApps = { currentScreen = Screen.AllApps }
    )
    Screen.AllApps -> AllAppsScreen(
        onBack = { currentScreen = Screen.Home }
    )
}
```

### Known Issue
**"All Apps" button doesn't navigate consistently**
- **Cause:** Screen state not properly hoisted to root; callbacks may not be wired correctly
- **Fix Approach:**
  - Ensure `currentScreen` state is defined at root composable level
  - Pass `onOpenAllApps()` and `onBack()` callbacks down to child composables
  - Verify button click triggers the callback
  - Use `when (currentScreen)` at root to render correct screen

---

## Design Philosophy
- **Minimalism** over features
- **Clarity** over flexibility
- **Big touch targets**, zero clutter
- **Predictable behavior** (no surprises)
- **Speed** over polish (initially)

---

## Priority Next Steps

### 1. Fix Navigation (High Priority)
- Ensure Home ↔ All Apps switching works reliably
- Debug state hoisting and callback wiring
- Test on both emulator and real device

### 2. All Apps UX Improvements
- Verify alphabetical sorting works correctly
- Consider adding search functionality (future)
- Optimize performance if scrolling is slow

### 3. Favourites UX Enhancements
- Add visual indicators for favourite status
- Consider reordering functionality (drag-and-drop)
- Test long-press removal on all devices

### 4. Performance & Stability
- Minimize unnecessary recompositions
- Clean up state handling
- Test with many installed apps

---

## Future Features (Optional)
- Contacts quick access
- Dialer shortcuts
- Emergency mode
- Accessibility mode (elderly-friendly)
- Gesture-based navigation
- Settings screen (toggle features)
- Default launcher support (system-level)

---

## Project Structure (Expected)
```
ClearPhone/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/...
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
└── settings.gradle.kts
```

---

## Key Testing Scenarios
- [ ] App launches and displays home screen
- [ ] Home screen shows favourite apps
- [ ] Long press removes a favourite (toast appears)
- [ ] "All Apps" button navigates to app list
- [ ] All Apps list is alphabetically sorted
- [ ] Launching an app from All Apps works
- [ ] Back button returns to home screen
- [ ] Favourites persist after app restart
- [ ] ClearPhone launcher is filtered from All Apps list

---

## Important Context for Development
- **Developer:** Angus (prefers practical steps, working code over theory)
- **Approach:** Simple solutions, clear explanations, avoid overengineering
- **Status:** Working prototype in active iterative development
- **Avoid:** Navigation Compose, complex frameworks, feature bloat

---

## Related Projects
- **VOID UI Icon Pack** — separate project; may integrate later as bundled minimal OS experience
- **Icon Packs App** — could use ClearPhone as launcher
- **Consolidated UI** — larger design direction

---

## Quick Reference

| Feature | Status | Notes |
|---------|--------|-------|
| Build & Run | ✅ | Works on emulator |
| Home Screen | ✅ | Displays favourites |
| All Apps Screen | ✅ | LazyColumn, alphabetical |
| Favourites Persistence | ✅ | DataStore |
| Navigation | ⚠️ | Needs debugging |
| System Launcher | ✅ | Registered via intent filters |

---

*Last Updated: 2026-04-24*
