![LOGO](candleflask-android/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png)

# CandleFlask

A streaming offline first US stock ticker Android app using the free tier of
[Tiingo API](https://api.tiingo.com/)

## Overview

- Clean architecture
- OkHttp WebSocket + Moshi with multivariate JSON parsing + Retrofit
- Device DayNight support (see ThemedTypedValues and attrs.xml)
- Latest AAC ViewModel + Jetpack Navigation
- Room
- Coroutine + StateFlows
- Hilt - Focus on only constructor-based injection. Non-injectable constructors are provided by delegate classes)
- Joda BigMoney (Feel free to look at the core tests as per why we don't use BigDecimal)
- Unit tests for `core`, `core-framework` (infrastructure), AAC ViewModels, and UI layer
- Sample integration test (at `core-framework` only for now)

## Pics

<img src="/graphics/home-night.png" width="256" /> <img src="/graphics/search-night.png" width="256" /> <img src="/graphics/settings-night.png" width="256" />

<img src="/graphics/daynight-switch.gif" width="360" />

## Upcoming

- Even more tests
- UI to ViewModel behavior tests
- Imports cleanups
- Gradle dependencies refactoring.
- Some of the dependencies duplications are initially intentional since, for the readers, it's faster scanning down what
  exactly the module uses.

## License

This project is provided under the [MIT License](LICENSE.md)