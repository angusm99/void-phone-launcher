# void phone — Play Store Listing Copy

**Build:** v1.1.3 (versionCode 6), targetSdk 36, minSdk 26 (Android 8.0+) · **Updated:** 2026-10-15
**Ready to paste into Play Console.** Facts below were measured from the build, not copied from older docs:
no internet permission (merged manifest checked with `aapt2 dump permissions`), no ads, analytics or
crash-reporting SDKs (runtime dependencies are AndroidX, Compose, DataStore and an Accompanist drawable
painter only), no in-app purchases.

> Third-party brand names are deliberately kept out of the description (Play treats lists of other
> companies' brands as keyword spam / impersonation risk). VOID UI is our own icon pack, so naming it is fine.
>
> "Return to previous launcher" is deliberately not advertised: it fires a plain Home intent, which resolves
> back to void phone once it is the default home app. The copy points people to the home-app setting instead.

---

## App name (30 max)

```
void phone – minimal launcher
```

Shorter alternative that matches the in-app label exactly: `void phone`

## Short description (80 max)

```
Your essential apps as circles on pure black. A calm, minimal AMOLED launcher.
```

## Full description (4000 max)

```
void phone is a quiet home screen for Android. Your essential apps sit in clean outlined circles on pure AMOLED black, with lowercase names and nothing else competing for your attention. No widgets, no badges, no feeds.

MAKE IT YOURS
• Choose the apps that matter: hold any app in All Apps to add it, hold a circle to remove it
• Circles, rounded squares or diamonds
• Small, medium or large tiles, in one, two or three columns
• Four accent colours: blue, green, pink and amber
• Show app names inside each shape, or switch to app icons

TERMINAL MODE
Flip the >_ switch for a retro CRT look: phosphor glow, scanlines, flicker and the occasional burst of static. App names become command prompts, and every few minutes a brief radiation warning flickers across the screen. Hold the title to rename the terminal header to anything you like.

ICON PACKS
void phone works with most icon packs made for other Android launchers. Pick one from the ⋮ menu and your app icons switch over. It was designed alongside the VOID UI icon pack, made for exactly this look.

EASY IN, EASY OUT
Make void phone your home screen from the ⋮ menu. Changed your mind? The same option takes you straight to Android's home-app setting, where you can pick any other launcher.

PRIVACY
No internet access. No account, no ads, no analytics, no tracking. Your favourite apps and settings are stored only on your device.
```

## Release notes — first release (500 max)

```
First release. Your essential apps as circles on pure black, with three shapes, three tile sizes, one to three columns, four accent colours, icon pack support and a retro CRT terminal mode.
```

---

## Graphics (in `play-store/`)

| Asset | File | Spec | Source |
|---|---|---|---|
| Hi-res icon | `icon-512.png` | 512×512, 24-bit PNG, no alpha | `scripts/make_store_icon.py` (mirrors the adaptive icon vectors) |
| Feature graphic | *not made yet* | 1024×500, 24-bit PNG, no alpha | — |
| Phone screenshots | `screenshot-0N-*.png` | 1080×1920, 24-bit PNG, no alpha | Captured on the API 36 emulator |

Screenshot rules: 2–8 images, each side 320–3840 px, long side no more than 2× the short side.
A Galaxy S21 Ultra capture (1080×2400, 2.22:1) would be rejected, which is why these come from the emulator at 1080×1920.

## Console settings

- Category: **Personalization**
- Tags: launcher, minimalist, AMOLED
- Contains ads: **No**
- In-app purchases: **No**
- App access: all functionality is available without special access (no login)
- Content rating: complete the questionnaire (no violence, user-generated content, location, purchases or gambling → expect Everyone / PEGI 3)
- Target audience: 13+ (not designed for children)
- Privacy policy URL: `https://angusm99.github.io/void-phone-launcher/privacy-policy.html` (live once GitHub Pages is enabled on `master` → `/docs`)
- Support email: the same address used for the VOID UI listing

## Data safety form

- **Does the app collect or share any of the required user data types?** No.
- Why that is correct: the app reads the list of installed apps and any chosen icon pack's icons, and stores favourites and display settings, but all of it is processed and kept on the device. Play's definition of "collected" is data transmitted off the device, and the app has no internet permission, so nothing can be transmitted.
- Optional Android device backup is controlled by the user and handled by the system, not by the app.

## Release path reminder

Personal (individual) Play Console accounts created after 13 November 2023 must run a **closed test with at least 12 testers opted in for 14 consecutive days** before production access can be requested. Upload the signed bundle to a closed testing track first to start that clock, then finish the listing during the test.
