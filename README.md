# Pamasahe 🚌

**Jeepney fare calculator para sa mga pasahero ng Pilipinas.**

A Kotlin Multiplatform (Android + iOS) app that computes jeepney fares from the
official LTFRB fare matrix — pick your ride and drop-off on a map (or type the
distance), set who's riding, and see the full fare breakdown so you know
exactly what you should be paying.

## Features

- **Traditional & Modern PUJ rates** — LTFRB fare matrix effective **March 19, 2026**:

  | | Base fare (first 4 km) | Per succeeding km |
  |---|---|---|
  | Traditional | ₱14.00 | ₱2.00 |
  | Modern | ₱17.00 | ₱2.40 |

- **Map picker** — tap your *sakayan* and *babaan* on a MapLibre map
  (OpenFreeMap tiles, no API key); route distance comes from the public OSRM
  router, with a straight-line ×1.3 estimate as offline fallback.
- **Manual distance entry** — no signal? Type the kilometers directly.
- **Group fares** — count Regular / Estudyante / Senior Citizen / PWD
  passengers separately; the 20% discount is applied automatically.
- **Itemized breakdown** — base fare, extra kilometers × rate, discounts, and
  the total, so overcharging is easy to spot.
- **Taglish UI** 🇵🇭

## Tech stack

| Layer | Library |
|---|---|
| UI (shared) | [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) 1.10.3 |
| Maps | [maplibre-compose](https://github.com/maplibre/maplibre-compose) 0.13.0 |
| Networking | [Ktor](https://ktor.io/) 3.5.1 |
| Routing | [OSRM public demo server](https://router.project-osrm.org) |
| Language | Kotlin 2.3.21 |

All fare logic lives in `commonMain` (`ph.jeepfare.domain`) as pure functions
with unit tests in `commonTest` — shared unchanged between Android and iOS.

## Download

Every push to `main` triggers the [Release APK workflow](.github/workflows/release.yml),
which runs the unit tests, builds the signed APKs, and publishes them on the
[Releases page](https://github.com/zorenkonte/Maps/releases) — grab the
**arm64-v8a** APK for a modern phone.

## Building

### Android

```sh
./gradlew :composeApp:assembleDebug     # debug APK
./gradlew :composeApp:assembleRelease   # release APKs (per-ABI + universal)
./gradlew :composeApp:testDebugUnitTest # unit tests
```

APKs land in `composeApp/build/outputs/apk/`. Release builds are signed with
the checked-in **self-signed keystore** (`keystore/pamasahe.jks`, passwords in
`composeApp/build.gradle.kts`) — good enough for sideloading; generate your own
keystore before publishing anywhere.

Note: MapLibre Android 13 renders with **Vulkan**, and its manifest marks
Vulkan 1.0 as required — fine for sideloading, but it filters out pre-2017
non-Vulkan devices on Google Play. If you need those devices, switch to the
`org.maplibre.gl:android-sdk-opengl` fallback per the
[maplibre-compose docs](https://maplibre.org/maplibre-compose/getting-started/).

### iOS

Requires macOS + Xcode. Open `iosApp/iosApp.xcodeproj` and run; the build phase
compiles the Kotlin framework via
`./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`, and the MapLibre
native framework is pulled in via Swift Package Manager. The iOS target has no
app icon asset catalog yet — add one in Xcode before shipping.

## Updating fare rates

When the LTFRB adjusts fares, edit the single table in
[`FareRules.kt`](composeApp/src/commonMain/kotlin/ph/jeepfare/domain/FareRules.kt)
and update the tests. Rounding conventions (nearest ₱0.25, per-started-km
charging) are named constants in the same file.

Fare sources: [LTFRB fare hike, March 2026 — Rappler](https://www.rappler.com/newsbreak/iq/fare-increase-jeepneys-buses-ride-hailing-philippines-march-19-2026/),
[Philippine News Agency](https://www.pna.gov.ph/articles/1271201).

## Disclaimer

Fares shown are computed from the published fare matrix and the measured route
distance; actual conductor fares may vary with the route's official distance
matrix. Not affiliated with the LTFRB.
