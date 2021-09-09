![LOGO](candleflask-android/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png)

# CandleFlask

A streaming offline first US stock ticker Android app based on
[Tiingo API](https://api.tiingo.com/) free tier.

## Overview

- Clean architecture
- OkHttp WebSocket + Moshi with multivariate JSON parsing + Retrofit
- Device DayNight support (
  see [ThemedTypedValues](candleflask-android/src/main/java/com/candleflask/android/ui/ThemedTypedValues.kt)
  and [attrs.xml](candleflask-android/src/main/res/values/attrs.xml))
- Latest AAC ViewModel + Jetpack Navigation + Room
- Coroutine + StateFlows
- Hilt - Focus on only constructor-based injection. Classes with non-injectable constructors are provided by delegate
  classes
- Joda BigMoney (Feel free to look at the `core` tests as per why we can't simply use BigDecimal)
- Unit tests for `core`, `core-framework` (infrastructure), AAC ViewModels and UI layers
- Sample integration test (at `core-framework` only for now)
- **StrictMode friendly**

## Pics

<img src="/graphics/home-night.png" width="256" /> <img src="/graphics/search-night.png" width="256" /> <img src="/graphics/settings-night.png" width="256" />

<img src="/graphics/daynight-switch.gif" width="360" />

## Musings

- There were indeed some IO calls during OkHttp initialization. Hence why any `OkHttpClient` injection is provided via
  a `Provider` instead
- Lifecycle `launch` is executing on  `Dispatchers.Main` due to `Lifecycle.addObserver` requirements
    - Any first access to AAC ViewModel in `suspend` blocks may incur IO thread penalty if you're not careful
    - Always opt for `lazy` whenever you're touching repository layers and below
- DayNight is surprisingly easy to implement once you start abusing `attrs`
- The current Gradle modules (`core`, `core-framework`, `candleflask-android`) still doesn't scream *correct* to me. I'm
  all ears for opinions.

## Upcoming

- Even more tests
- UI to ViewModel behavior tests
- Imports cleanups
- Gradle dependencies refactoring
    - Some of the dependencies duplications are temporarily intentional since, for the readers (me), it's simply faster
      scanning down the configs

## License

This project is provided under the [Apache 2.0 License](LICENSE.md)