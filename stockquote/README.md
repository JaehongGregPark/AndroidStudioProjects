# StockQuoteApp

Sample Android Studio project for checking stock quotes.

## Features

- Search by stock symbol
- Show current price, daily change, and change percent
- Display exchange, currency, and last market update time
- Handle loading and error states

## Example symbols

- `AAPL`
- `MSFT`
- `GOOG`
- `TSLA`
- `005930.KS`

## Data source

The app uses the Yahoo Finance chart endpoint:

- `https://query1.finance.yahoo.com/v8/finance/chart/AAPL?interval=1d&range=1d`

This is convenient for a sample app because it does not require an API key, but it is not an official paid market-data API. Review its suitability before using it in production.

## Open in Android Studio

1. Open this folder in Android Studio.
2. Run Gradle sync.
3. Start an emulator or connect a device.
4. Run the `app` configuration.

## Build setup

The project is configured for a modern Android toolchain:

- Android Gradle Plugin `8.5.2`
- Kotlin `1.9.24`
- Gradle `8.7`
- Java target `17`

This setup is compatible with newer Android Studio installations and avoids the older Gradle 7.5 / Java 21 sync issue.
