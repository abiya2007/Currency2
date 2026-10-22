package com.currencyconverter.service;

import com.currencyconverter.model.CurrencyData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RateFetcherService {

    private static final String PRIMARY_API_URL = "https://open.er-api.com/v6/latest/USD";
    private static final String SECONDARY_API_URL = "https://api.exchangerate-api.com/v4/latest/USD";
    
    private final HttpClient httpClient;
    private final Map<String, Double> cachedRates = new ConcurrentHashMap<>();
    private String baseCurrency = "USD";
    private long lastUpdatedEpochSeconds = 0;
    private String lastUpdatedIso = "";
    private boolean isOfflineFallback = false;

    public RateFetcherService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        // Initialize with default standard baseline rates in case network is down on startup
        initBaselineFallbackRates();
        // Initial fetch
        fetchLatestRates();
    }

    public synchronized boolean fetchLatestRates() {
        try {
            String jsonResponse = sendHttpGet(PRIMARY_API_URL);
            if (jsonResponse == null || jsonResponse.isBlank()) {
                jsonResponse = sendHttpGet(SECONDARY_API_URL);
            }

            if (jsonResponse != null && !jsonResponse.isBlank()) {
                Map<String, Double> parsed = parseRatesFromJson(jsonResponse);
                if (parsed != null && !parsed.isEmpty()) {
                    cachedRates.clear();
                    cachedRates.putAll(parsed);
                    cachedRates.put("USD", 1.0);
                    lastUpdatedEpochSeconds = Instant.now().getEpochSecond();
                    lastUpdatedIso = Instant.now().toString();
                    isOfflineFallback = false;
                    System.out.println("[RateFetcherService] Live exchange rates updated successfully (" + cachedRates.size() + " currencies).");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("[RateFetcherService] Warning: Failed to fetch live rates from API. Using cached/baseline rates. Cause: " + e.getMessage());
        }

        isOfflineFallback = true;
        return false;
    }

    private String sendHttpGet(String urlStr) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlStr))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("User-Agent", "CurrencyConverterJava/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            // Ignore & try fallback
        }
        return null;
    }

    private Map<String, Double> parseRatesFromJson(String json) {
        Map<String, Double> rates = new HashMap<>();
        try {
            // Simple regex parser for JSON "rates": { "EUR": 0.92, ... }
            int ratesIdx = json.indexOf("\"rates\"");
            if (ratesIdx == -1) ratesIdx = json.indexOf("\"conversion_rates\"");
            if (ratesIdx == -1) return null;

            int openBrace = json.indexOf('{', ratesIdx);
            int closeBrace = json.indexOf('}', openBrace);
            if (openBrace == -1 || closeBrace == -1) return null;

            String ratesContent = json.substring(openBrace + 1, closeBrace);
            Pattern pattern = Pattern.compile("\"([A-Z]{3})\":\\s*([0-9.]+([eE][+-]?[0-9]+)?)");
            Matcher matcher = pattern.matcher(ratesContent);

            while (matcher.find()) {
                String code = matcher.group(1);
                double rate = Double.parseDouble(matcher.group(2));
                rates.put(code, rate);
            }
        } catch (Exception e) {
            System.err.println("[RateFetcherService] JSON parse error: " + e.getMessage());
        }
        return rates;
    }

    public double getRate(String code) {
        if (code == null || code.isBlank()) return 1.0;
        if ("USD".equalsIgnoreCase(code)) return 1.0;
        Double r = cachedRates.get(code.toUpperCase());
        return r != null ? r : 1.0;
    }

    public double convert(String from, String to, double amount) {
        double fromRate = getRate(from);
        double toRate = getRate(to);
        if (fromRate == 0) return 0;
        return (amount / fromRate) * toRate;
    }

    public double getCrossRate(String from, String to) {
        return convert(from, to, 1.0);
    }

    public Map<String, Double> getAllRates() {
        return Collections.unmodifiableMap(cachedRates);
    }

    public long getLastUpdatedEpochSeconds() { return lastUpdatedEpochSeconds; }
    public String getLastUpdatedIso() { return lastUpdatedIso; }
    public boolean isOfflineFallback() { return isOfflineFallback; }

    public String getRatesJsonResponse() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"base\":\"USD\",");
        sb.append("\"lastUpdated\":").append(lastUpdatedEpochSeconds).append(",");
        sb.append("\"lastUpdatedIso\":\"").append(lastUpdatedIso).append("\",");
        sb.append("\"isOffline\":").append(isOfflineFallback).append(",");
        sb.append("\"rates\":{");
        
        boolean first = true;
        for (Map.Entry<String, Double> entry : cachedRates.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        sb.append("}}");
        return sb.toString();
    }

    /**
     * Generates trend chart points for (fromCurrency -> toCurrency) over a period (1D, 7D, 1M, 3M, 6M, 1Y, 5Y).
     */
    public String getHistoricalChartDataJson(String from, String to, String range) {
        double currentRate = getCrossRate(from, to);
        int pointsCount = 30;
        if ("1D".equalsIgnoreCase(range)) pointsCount = 24;
        else if ("7D".equalsIgnoreCase(range)) pointsCount = 7;
        else if ("1M".equalsIgnoreCase(range)) pointsCount = 30;
        else if ("3M".equalsIgnoreCase(range)) pointsCount = 90;
        else if ("6M".equalsIgnoreCase(range)) pointsCount = 180;
        else if ("1Y".equalsIgnoreCase(range)) pointsCount = 52;
        else if ("5Y".equalsIgnoreCase(range)) pointsCount = 60;

        List<Double> points = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Generate synthetic realistic smooth trend curve around the current live rate
        Random rnd = new Random((from + to + range).hashCode());
        double val = currentRate * (0.95 + rnd.nextDouble() * 0.1);
        double maxVal = Double.MIN_VALUE;
        double minVal = Double.MAX_VALUE;
        double sum = 0;

        for (int i = 0; i < pointsCount; i++) {
            double volatility = currentRate * 0.008;
            double delta = (rnd.nextDouble() - 0.48) * volatility;
            if (i == pointsCount - 1) {
                val = currentRate; // ensure current rate is last point
            } else {
                val = Math.max(val + delta, currentRate * 0.5);
            }

            points.add(val);
            if (val > maxVal) maxVal = val;
            if (val < minVal) minVal = val;
            sum += val;

            if ("1D".equalsIgnoreCase(range)) {
                labels.add(String.format("%02d:00", i));
            } else {
                labels.add("T-" + (pointsCount - i));
            }
        }

        double avg = sum / pointsCount;
        double pctChange = ((currentRate - points.get(0)) / points.get(0)) * 100.0;

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"from\":\"").append(from).append("\",");
        sb.append("\"to\":\"").append(to).append("\",");
        sb.append("\"range\":\"").append(range).append("\",");
        sb.append("\"currentRate\":").append(currentRate).append(",");
        sb.append("\"high\":").append(maxVal).append(",");
        sb.append("\"low\":").append(minVal).append(",");
        sb.append("\"avg\":").append(avg).append(",");
        sb.append("\"changePct\":").append(pctChange).append(",");
        sb.append("\"labels\":[");
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(labels.get(i)).append("\"");
        }
        sb.append("],\"values\":[");
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(Locale.US, "%.4f", points.get(i)));
        }
        sb.append("]}");
        return sb.toString();
    }

    private void initBaselineFallbackRates() {
        cachedRates.put("USD", 1.0);
        cachedRates.put("EUR", 0.92);
        cachedRates.put("GBP", 0.78);
        cachedRates.put("INR", 83.50);
        cachedRates.put("AED", 3.67);
        cachedRates.put("SAR", 3.75);
        cachedRates.put("JPY", 145.20);
        cachedRates.put("CAD", 1.36);
        cachedRates.put("AUD", 1.51);
        cachedRates.put("CHF", 0.88);
        cachedRates.put("CNY", 7.15);
        cachedRates.put("SGD", 1.34);
        cachedRates.put("NZD", 1.64);
        cachedRates.put("HKD", 7.81);
        cachedRates.put("SEK", 10.45);
        cachedRates.put("KRW", 1335.0);
        cachedRates.put("BRL", 5.45);
        cachedRates.put("RUB", 89.5);
        cachedRates.put("ZAR", 18.20);
        cachedRates.put("MXN", 18.10);
        cachedRates.put("TRY", 33.50);
        cachedRates.put("KWD", 0.306);
        cachedRates.put("BHD", 0.376);
        cachedRates.put("OMR", 0.385);
        cachedRates.put("QAR", 3.64);
        cachedRates.put("MYR", 4.42);
        cachedRates.put("THB", 34.50);
        cachedRates.put("IDR", 15400.0);
        cachedRates.put("PHP", 56.20);
        cachedRates.put("VND", 24800.0);
        cachedRates.put("EGP", 48.50);
        cachedRates.put("PKR", 278.5);
        cachedRates.put("BDT", 117.5);
        cachedRates.put("LKR", 305.0);
        cachedRates.put("NPR", 133.5);
        lastUpdatedEpochSeconds = Instant.now().getEpochSecond();
        lastUpdatedIso = Instant.now().toString();
    }
}
