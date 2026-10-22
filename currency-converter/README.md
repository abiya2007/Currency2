# Global Currency Converter & FX Exchange Web Application (Java 21)

A modern, responsive, professional **Currency Converter Web Application** built with a **Java 21 backend** and a **responsive Fintech Dashboard frontend**. It enables real-time money conversion between **any supported currencies worldwide** (160+ currencies) using live exchange rates, automated background fetching, rate alerts, interactive charts, multi-currency conversion, conversion history, multi-language support (English, Malayalam, Hindi, Arabic, Spanish, French), offline support, dark/light themes, and PWA capabilities.

---

## 💻 Running & Debugging in Visual Studio Code (VS Code)

This repository is fully configured for **Visual Studio Code (VS Code)** with ready-to-use launch configurations, build tasks, and workspace settings.

### Step-by-Step for VS Code:

1. **Open Project in VS Code**:
   - Launch VS Code.
   - Go to `File` > `Open Folder...` (or press `Ctrl+K Ctrl+O`) and select `c:\Users\Administrator\Downloads\Projects\currency converter`.

2. **Run/Debug via F5 (One-Click Launch)**:
   - Open [`CurrencyConverterServer.java`](file:///c:/Users/Administrator/Downloads/Projects/currency%20converter/src/main/java/com/currencyconverter/CurrencyConverterServer.java).
   - Press **`F5`** (or click **`Run` > `Start Debugging`**).
   - Alternatively, click the **`Run`** codelens button above `public static void main(String[] args)`.

3. **Run via VS Code Build Task (`Ctrl+Shift+B`)**:
   - Press **`Ctrl+Shift+B`** to automatically compile the Java project into `bin/`.
   - Open integrated terminal (`Ctrl+\``) and run `.\run.bat` or `.\run.ps1`.

4. **Access in Browser**:
   - Open **`http://localhost:8080`** in your browser.

---

## 🌟 Key Features

1. **Main Currency Converter**:
   - Amount input supporting decimals & large numbers.
   - Quick amount buttons ($10, $50, $100, $500, $1,000, $5,000).
   - "From" & "To" searchable currency selectors displaying **Country Flags**, **Currency Codes**, **Names**, and **Symbols**.
   - **⇄ Swap Currencies** button with smooth rotation animation.
   - Real-time conversion display with live exchange rate and last-updated timestamp.
   - Action buttons for Copying 📋, Sharing 🔗, Printing 🖨️, and Bookmarking Favorite pairs ⭐.

2. **Live Exchange Rates**:
   - Automatic fetching of real-time exchange rates via reliable keyless public APIs (`open.er-api.com` primary, `exchangerate-api`, `frankfurter` fallbacks).
   - Configurable auto-refresh frequency (1m, 5m, 15m, 30m).
   - Graceful API error handling with cached rate fallbacks and visual status indicators (LIVE vs CACHED).

3. **Searchable Currency Modal**:
   - Fast instant filtering by **Currency Code** (e.g. `INR`, `USD`), **Name** (e.g. `Rupee`, `Euro`), or **Country Name** (e.g. `India`, `United States`).

4. **Multi-Currency Calculator**:
   - Enter one base amount (e.g. 100 USD) and view equivalent values simultaneously across multiple currencies (`INR`, `EUR`, `GBP`, `AED`, `JPY`, etc.).
   - Dynamically add or remove target currencies.

5. **Interactive Exchange Rate Trends & Charts**:
   - Powered by Chart.js with timeframe controls: **1D**, **7D**, **1M**, **3M**, **6M**, **1Y**, **5Y**.
   - Automated metrics computation: **Highest Rate**, **Lowest Rate**, **Average Rate**, and **Percentage Change**.

6. **Rate Alerts Engine**:
   - Dedicated alert creation for any pair (e.g. USD → INR) with custom target rate and conditions (**Rises Above ≥** or **Drops Below ≤**).
   - Background Java `AlertEngine` monitoring rates every minute and emitting notifications.

7. **Conversion History**:
   - Logs Date/Time, Pair, Base Amount, Exchange Rate, and Converted Amount.
   - Delete individual records, clear all history, or export to **CSV**.

8. **Multi-Language Support (i18n)**:
   - Built-in support for **6 Languages**:
     - 🇺🇸 **English** (en)
     - 🇮🇳 **Malayalam** (ml - മലയാളം)
     - 🇮🇳 **Hindi** (hi - हिंदी)
     - 🇦🇪 **Arabic** (ar - العربية) *(with full Right-to-Left RTL layout)*
     - 🇪🇸 **Spanish** (es - Español)
     - 🇫🇷 **French** (fr - Français)

9. **Offline Support (PWA)**:
   - Integrated Service Worker (`sw.js`) and local caching.
   - Detects network offline state and presents last cached rates with explicit "CACHED" badge and timestamp.

10. **Dark & Light Mode**:
    - Smooth theme toggle with persisted local preference.

---

## 📁 Project Folder Structure

```
currency converter/
├── .vscode/                                  # VS Code Configuration Folder
│   ├── launch.json                           # F5 Debug / Launch Configuration
│   ├── tasks.json                            # Build Tasks (Ctrl+Shift+B)
│   ├── settings.json                         # Workspace & Java Settings
│   └── extensions.json                       # Recommended Java Extensions
├── pom.xml                                   # Maven configuration file
├── run.bat                                   # Windows Batch launcher script
├── run.ps1                                   # PowerShell launcher script
├── mvnw.cmd                                  # Maven Wrapper script
├── README.md                                 # Complete documentation
├── data/                                     # Persistent JSON file database
│   ├── history.json                          # Saved conversion history
│   ├── alerts.json                           # Active rate alerts
│   ├── notifications.json                    # System notification logs
│   ├── favorites.json                        # Favorite currencies
│   └── settings.json                         # User settings
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── currencyconverter/
        │           ├── CurrencyConverterServer.java  # Main Java HttpServer entry point
        │           ├── controller/
        │           │   └── ApiController.java        # REST API router & handlers
        │           ├── model/
        │           │   └── CurrencyData.java         # 160+ currency dictionary metadata
        │           ├── service/
        │           │   ├── RateFetcherService.java   # HttpClient live API fetcher & chart engine
        │           │   └── AlertEngine.java          # Background alert monitoring worker
        │           └── store/
        │               └── AlertAndHistoryStore.java # Database persistence layer
        └── resources/
            └── static/
                ├── index.html                # Single Page Application HTML markup
                ├── styles.css                # Responsive Fintech UI CSS stylesheet
                ├── app.js                    # Main frontend SPA JavaScript controller
                ├── i18n.js                   # Translations dictionary & language engine
                └── sw.js                     # Service worker for offline caching
```

---

## 🔌 REST API Reference

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `GET /api/currencies` | `GET` | Returns 160+ currency metadata (code, name, symbol, flag, country). |
| `GET /api/rates` | `GET` | Returns latest exchange rates relative to USD, timestamp, and offline status. |
| `GET /api/convert?from=USD&to=INR&amount=100` | `GET` | Computes conversion, rates, symbols, flags, and records history. |
| `GET /api/history` | `GET` | Returns saved conversion history log. |
| `DELETE /api/history?id={id}` | `DELETE` | Deletes specific history item or clears all if `id` omitted. |
| `GET /api/alerts` | `GET` | Returns active rate alerts. |
| `POST /api/alerts` | `POST` | Creates a new rate alert. |
| `DELETE /api/alerts?id={id}` | `DELETE` | Removes a rate alert. |
| `GET /api/historical-rates?pair=USD_INR&range=1M` | `GET` | Generates historical trend data points for Chart.js. |
| `GET /api/health` | `GET` | Server health check endpoint. |
