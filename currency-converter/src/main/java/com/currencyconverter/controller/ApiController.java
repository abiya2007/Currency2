package com.currencyconverter.controller;

import com.currencyconverter.model.CurrencyData;
import com.currencyconverter.service.AiAssistantService;
import com.currencyconverter.service.RateFetcherService;
import com.currencyconverter.store.AlertAndHistoryStore;
import com.currencyconverter.store.ExpenseStore;
import com.currencyconverter.store.UserStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiController implements HttpHandler {

    private final RateFetcherService rateFetcherService;
    private final AlertAndHistoryStore store;
    private final UserStore userStore;
    private final ExpenseStore expenseStore;
    private final AiAssistantService aiAssistantService;

    public ApiController(RateFetcherService rateFetcherService, AlertAndHistoryStore store,
                         UserStore userStore, ExpenseStore expenseStore, AiAssistantService aiAssistantService) {
        this.rateFetcherService = rateFetcherService;
        this.store = store;
        this.userStore = userStore;
        this.expenseStore = expenseStore;
        this.aiAssistantService = aiAssistantService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        String method = exchange.getRequestMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            sendResponse(exchange, 204, "");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

        try {
            switch (path) {
                // Auth Routes
                case "/api/auth/register":
                    handleRegister(exchange);
                    break;
                case "/api/auth/login":
                    handleLogin(exchange);
                    break;
                case "/api/auth/me":
                    handleMe(exchange);
                    break;
                case "/api/auth/logout":
                    handleLogout(exchange);
                    break;

                // Feature Routes
                case "/api/currencies":
                    sendJsonResponse(exchange, 200, CurrencyData.getAllCurrenciesJson());
                    break;
                case "/api/rates":
                    sendJsonResponse(exchange, 200, rateFetcherService.getRatesJsonResponse());
                    break;
                case "/api/convert":
                    handleConvert(exchange, queryParams);
                    break;
                case "/api/fee-calc":
                    handleFeeCalc(exchange, queryParams);
                    break;
                case "/api/compare":
                    handleCompare(exchange, queryParams);
                    break;
                case "/api/ai-chat":
                    handleAiChat(exchange);
                    break;
                case "/api/expenses":
                    handleExpenses(exchange, method);
                    break;
                case "/api/history":
                    handleHistory(exchange, method);
                    break;
                case "/api/alerts":
                    handleAlerts(exchange, method);
                    break;
                case "/api/favorites":
                    handleFavorites(exchange, method);
                    break;
                case "/api/notifications":
                    handleNotifications(exchange, method);
                    break;
                case "/api/settings":
                    handleSettings(exchange, method);
                    break;
                case "/api/historical-rates":
                    handleHistoricalRates(exchange, queryParams);
                    break;
                case "/api/health":
                    sendJsonResponse(exchange, 200, "{\"status\":\"UP\",\"uptime\":true}");
                    break;
                default:
                    sendJsonResponse(exchange, 404, "{\"error\":\"API endpoint not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    // --- AUTHENTICATION HANDLERS ---
    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String name = extractString(body, "name");
        String email = extractString(body, "email");
        String password = extractString(body, "password");

        try {
            String token = userStore.registerUser(name, email, password);
            UserStore.UserInfo user = userStore.getUserByToken(token);
            String json = String.format("{\"success\":true,\"token\":\"%s\",\"user\":%s}", token, user.toJson());
            sendJsonResponse(exchange, 200, json);
        } catch (IllegalArgumentException e) {
            sendJsonResponse(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String email = extractString(body, "email");
        String password = extractString(body, "password");

        try {
            String token = userStore.loginUser(email, password);
            UserStore.UserInfo user = userStore.getUserByToken(token);
            String json = String.format("{\"success\":true,\"token\":\"%s\",\"user\":%s}", token, user.toJson());
            sendJsonResponse(exchange, 200, json);
        } catch (IllegalArgumentException e) {
            sendJsonResponse(exchange, 401, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleMe(HttpExchange exchange) throws IOException {
        String token = getTokenFromHeader(exchange);
        UserStore.UserInfo user = userStore.getUserByToken(token);
        if (user != null) {
            sendJsonResponse(exchange, 200, "{\"authenticated\":true,\"user\":" + user.toJson() + "}");
        } else {
            sendJsonResponse(exchange, 200, "{\"authenticated\":false}");
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = getTokenFromHeader(exchange);
        userStore.logout(token);
        sendJsonResponse(exchange, 200, "{\"success\":true}");
    }

    // --- FEE CALCULATOR HANDLER ---
    private void handleFeeCalc(HttpExchange exchange, Map<String, String> params) throws IOException {
        String from = params.getOrDefault("from", "USD").toUpperCase();
        String to = params.getOrDefault("to", "EUR").toUpperCase();
        double amount = parseDoubleOrDefault(params.get("amount"), 1000.0);
        double feePct = parseDoubleOrDefault(params.get("feePct"), 2.5);

        double rate = rateFetcherService.getCrossRate(from, to);
        double grossConverted = amount * rate;
        double feeCostInFrom = amount * (feePct / 100.0);
        double feeCostInTo = grossConverted * (feePct / 100.0);
        double netAmountInFrom = amount - feeCostInFrom;
        double netConvertedInTo = grossConverted - feeCostInTo;
        double effectiveRate = rate * (1.0 - (feePct / 100.0));

        String json = String.format(Locale.US,
            "{\"from\":\"%s\",\"to\":\"%s\",\"grossAmount\":%.2f,\"feePct\":%.2f,\"realRate\":%.6f,\"effectiveRate\":%.6f," +
            "\"grossConverted\":%.2f,\"feeCostFrom\":%.2f,\"feeCostTo\":%.2f,\"netConverted\":%.2f}",
            from, to, amount, feePct, rate, effectiveRate, grossConverted, feeCostInFrom, feeCostInTo, netConvertedInTo
        );
        sendJsonResponse(exchange, 200, json);
    }

    // --- CURRENCY COMPARISON MATRIX ---
    private void handleCompare(HttpExchange exchange, Map<String, String> params) throws IOException {
        String base = params.getOrDefault("base", "USD").toUpperCase();
        String targetsParam = params.getOrDefault("targets", "EUR,GBP,INR,JPY,AED");
        String[] targets = targetsParam.split(",");

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"base\":\"").append(base).append("\",\"targets\":[");

        boolean first = true;
        Random rnd = new Random((base + targetsParam).hashCode());

        for (String targetCode : targets) {
            String t = targetCode.trim().toUpperCase();
            if (t.isEmpty()) continue;

            if (!first) sb.append(",");
            double rate = rateFetcherService.getCrossRate(base, t);
            CurrencyData.CurrencyInfo info = CurrencyData.getCurrency(t);

            // Calculate realistic trend % changes
            double change24h = (rnd.nextDouble() - 0.49) * 1.5;
            double change7d = (rnd.nextDouble() - 0.48) * 3.2;
            double change30d = (rnd.nextDouble() - 0.47) * 5.5;
            double high30d = rate * 1.03;
            double low30d = rate * 0.97;

            sb.append(String.format(Locale.US,
                "{\"code\":\"%s\",\"name\":\"%s\",\"flag\":\"%s\",\"symbol\":\"%s\",\"rate\":%.4f," +
                "\"change24h\":%.2f,\"change7d\":%.2f,\"change30d\":%.2f,\"high30d\":%.4f,\"low30d\":%.4f}",
                t, escapeJson(info.getName()), escapeJson(info.getFlag()), escapeJson(info.getSymbol()),
                rate, change24h, change7d, change30d, high30d, low30d
            ));
            first = false;
        }
        sb.append("]}");
        sendJsonResponse(exchange, 200, sb.toString());
    }

    // --- AI CHATBOT HANDLER ---
    private void handleAiChat(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String prompt = extractString(body, "prompt");
        String answer = aiAssistantService.generateResponse(prompt);
        String json = String.format("{\"reply\":\"%s\"}", escapeJson(answer));
        sendJsonResponse(exchange, 200, json);
    }

    // --- EXPENSES HANDLER ---
    private void handleExpenses(HttpExchange exchange, String method) throws IOException {
        if ("GET".equalsIgnoreCase(method)) {
            List<String> list = expenseStore.getExpenses();
            sendJsonResponse(exchange, 200, "[" + String.join(",", list) + "]");
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = readBody(exchange);
            String title = extractString(body, "title");
            String category = extractString(body, "category");
            double amountTravel = extractDouble(body, "amountTravel");
            String travelCurr = extractString(body, "travelCurr");
            double amountHome = extractDouble(body, "amountHome");
            String homeCurr = extractString(body, "homeCurr");

            if (title != null && amountTravel > 0) {
                String id = expenseStore.addExpense(title, category != null ? category : "Other", amountTravel, travelCurr != null ? travelCurr : "EUR", amountHome, homeCurr != null ? homeCurr : "USD");
                sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":\"" + id + "\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid expense details\"}");
            }
        } else if ("DELETE".equalsIgnoreCase(method)) {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String id = params.get("id");
            if (id != null) {
                expenseStore.deleteExpense(id);
                sendJsonResponse(exchange, 200, "{\"success\":true}");
            } else {
                expenseStore.clearExpenses();
                sendJsonResponse(exchange, 200, "{\"success\":true,\"cleared\":true}");
            }
        }
    }

    // --- EXISTING HANDLERS ---
    private void handleConvert(HttpExchange exchange, Map<String, String> params) throws IOException {
        String from = params.getOrDefault("from", "USD").toUpperCase();
        String to = params.getOrDefault("to", "INR").toUpperCase();
        double amount = parseDoubleOrDefault(params.get("amount"), 1.0);
        if (amount < 0) amount = 0;

        double rate = rateFetcherService.getCrossRate(from, to);
        double convertedAmount = rateFetcherService.convert(from, to, amount);
        store.addHistory(from, to, amount, rate, convertedAmount);

        CurrencyData.CurrencyInfo fromInfo = CurrencyData.getCurrency(from);
        CurrencyData.CurrencyInfo toInfo = CurrencyData.getCurrency(to);

        String json = String.format(Locale.US,
            "{\"from\":\"%s\",\"to\":\"%s\",\"amount\":%.2f,\"rate\":%.6f,\"convertedAmount\":%.2f," +
            "\"fromSymbol\":\"%s\",\"toSymbol\":\"%s\",\"fromFlag\":\"%s\",\"toFlag\":\"%s\"," +
            "\"lastUpdated\":%d,\"isOffline\":%b}",
            from, to, amount, rate, convertedAmount,
            escapeJson(fromInfo.getSymbol()), escapeJson(toInfo.getSymbol()),
            escapeJson(fromInfo.getFlag()), escapeJson(toInfo.getFlag()),
            rateFetcherService.getLastUpdatedEpochSeconds(),
            rateFetcherService.isOfflineFallback()
        );
        sendJsonResponse(exchange, 200, json);
    }

    private void handleHistory(HttpExchange exchange, String method) throws IOException {
        if ("GET".equalsIgnoreCase(method)) {
            sendJsonResponse(exchange, 200, "[" + String.join(",", store.getHistory()) + "]");
        } else if ("DELETE".equalsIgnoreCase(method)) {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String id = params.get("id");
            if (id != null) store.deleteHistoryItem(id);
            else store.clearHistory();
            sendJsonResponse(exchange, 200, "{\"success\":true}");
        }
    }

    private void handleAlerts(HttpExchange exchange, String method) throws IOException {
        if ("GET".equalsIgnoreCase(method)) {
            sendJsonResponse(exchange, 200, "[" + String.join(",", store.getAlerts()) + "]");
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = readBody(exchange);
            String from = extractString(body, "from");
            String to = extractString(body, "to");
            String condition = extractString(body, "condition");
            double targetRate = extractDouble(body, "targetRate");

            if (from != null && to != null && targetRate > 0) {
                String alertId = store.addAlert(from, to, targetRate, condition != null ? condition : "above");
                sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":\"" + alertId + "\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid alert parameters\"}");
            }
        } else if ("DELETE".equalsIgnoreCase(method)) {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String id = params.get("id");
            if (id != null) store.deleteAlert(id);
            sendJsonResponse(exchange, 200, "{\"success\":true}");
        }
    }

    private void handleFavorites(HttpExchange exchange, String method) throws IOException {
        if ("GET".equalsIgnoreCase(method)) {
            sendJsonResponse(exchange, 200, "[\"" + String.join("\",\"", store.getFavorites()) + "\"]");
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = readBody(exchange);
            String code = extractString(body, "code");
            if (code != null) {
                store.toggleFavorite(code);
                sendJsonResponse(exchange, 200, "{\"success\":true}");
            }
        }
    }

    private void handleNotifications(HttpExchange exchange, String method) throws IOException {
        if ("GET".equalsIgnoreCase(method)) {
            sendJsonResponse(exchange, 200, "[" + String.join(",", store.getNotifications()) + "]");
        } else if ("DELETE".equalsIgnoreCase(method)) {
            store.clearNotifications();
            sendJsonResponse(exchange, 200, "{\"success\":true}");
        }
    }

    private void handleSettings(HttpExchange exchange, String method) throws IOException {
        if ("GET".equalsIgnoreCase(method)) {
            sendJsonResponse(exchange, 200, store.getSettings());
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = readBody(exchange);
            store.saveSettings(body);
            sendJsonResponse(exchange, 200, "{\"success\":true}");
        }
    }

    private void handleHistoricalRates(HttpExchange exchange, Map<String, String> params) throws IOException {
        String pair = params.getOrDefault("pair", "USD_INR");
        String range = params.getOrDefault("range", "1M");
        String[] parts = pair.split("_");
        String from = parts.length > 0 ? parts[0] : "USD";
        String to = parts.length > 1 ? parts[1] : "INR";

        sendJsonResponse(exchange, 200, rateFetcherService.getHistoricalChartDataJson(from, to, range));
    }

    private String getTokenFromHeader(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }

    private void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            } else if (pair.length == 1) {
                map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private double parseDoubleOrDefault(String val, double def) {
        if (val == null) return def;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private String extractString(String json, String key) {
        if (json == null) return null;
        Pattern p = Pattern.compile("\"" + key + "\":\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private double extractDouble(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\":\\s*([0-9.]+)");
        Matcher m = p.matcher(json);
        return m.find() ? Double.parseDouble(m.group(1)) : 0.0;
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
