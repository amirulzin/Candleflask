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
- Hilt - Focus on _only_ constructor-based injection. Classes with non-injectable constructors are provided by delegate
  classes (
  e.g [DelegatedDispatchers](candleflask-android/src/main/java/com/candleflask/android/di/DelegatedDispatchers.kt))
- Joda BigMoney (Feel free to look at some of the historical `core` tests for case failures with BigDecimal)
- Various unit tests for `core`, `core-framework` (infrastructure), AAC ViewModels and UI layers
- Sample integration test (at `core-framework` only for now)
- **StrictMode friendly** (
  refer [StrictModeDefaults](candleflask-android/src/main/java/common/android/strictmode/StrictModeDefaults.kt))

## Pics

<img src="/graphics/home-night.png" width="256" /> <img src="/graphics/search-night.png" width="256" /> <img src="/graphics/settings-night.png" width="256" />

<img src="/graphics/daynight-switch.gif" width="360" />

## Musings

- There were indeed some IO calls during OkHttp initialization. Hence why any `OkHttpClient` injection is provided via
  a `Provider` instead
- Lifecycle `launch` is executing on  `Dispatchers.Main` due to `Lifecycle.addObserver` requirements
    - Any first access to AAC ViewModel in `suspend` blocks may incur IO thread penalty if you're not careful
    - Thus, always opt for `lazy` whenever you're touching repository layers and below
- DayNight is surprisingly easy to implement once you start abusing `attrs`
- The current Gradle modules (`core`, `core-framework`, `candleflask-android`) still doesn't scream *correct* to me. I'm
  all ears for opinions.

## Upcoming

- Even more unit tests
- UI to ViewModel behavior tests

## Building

All contributions are welcomed. Simply branch out from`develop`, follow the current commit messages style, squash, and
submit your Pull Request.

For integration tests, you can add your own Tiingo API key in the project `local.properties` via

```properties
INTEGRATION_TEST_API_KEY=myKeyValue
```

Run `./gradlew testDebug` to run all tests.

## Minor Notes

Since it was wired manually from scratch, feel free to extract
the [Tiingo datasource packages](core-framework/src/main/java/com/candleflask/framework/data/datasource/tiingo)
into your own projects/libraries. A Tiingo library was planned initially but scrapped due to lack of time. Therefore the
models were intentionally left as exhaustive as possible as per the
latest [API spec](https://api.tiingo.com/documentation/).

## License

This project is provided under the [Apache 2.0 License](LICENSE.md)