package com.currencyconverter.store;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AlertAndHistoryStore {

    private final Path dataDir;
    private final Path historyFile;
    private final Path alertsFile;
    private final Path notificationsFile;
    private final Path favoritesFile;
    private final Path settingsFile;

    private final List<String> historyList = new CopyOnWriteArrayList<>();
    private final List<String> alertsList = new CopyOnWriteArrayList<>();
    private final List<String> notificationsList = new CopyOnWriteArrayList<>();
    private final List<String> favoritesList = new CopyOnWriteArrayList<>();
    private String userSettingsJson = "{\"defaultCurrency\":\"USD\",\"precision\":2,\"refreshInterval\":5,\"theme\":\"light\",\"language\":\"en\",\"pushNotifications\":true}";

    public AlertAndHistoryStore() {
        this.dataDir = Paths.get("data");
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }

        this.historyFile = dataDir.resolve("history.json");
        this.alertsFile = dataDir.resolve("alerts.json");
        this.notificationsFile = dataDir.resolve("notifications.json");
        this.favoritesFile = dataDir.resolve("favorites.json");
        this.settingsFile = dataDir.resolve("settings.json");

        loadAll();
    }

    private void loadAll() {
        loadList(historyFile, historyList);
        loadList(alertsFile, alertsList);
        loadList(notificationsFile, notificationsList);
        loadList(favoritesFile, favoritesList);

        if (favoritesList.isEmpty()) {
            favoritesList.addAll(Arrays.asList("USD", "EUR", "GBP", "INR", "AED"));
            saveFavorites();
        }

        if (Files.exists(settingsFile)) {
            try {
                userSettingsJson = Files.readString(settingsFile);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void loadList(Path file, List<String> list) {
        if (Files.exists(file)) {
            try {
                List<String> lines = Files.readAllLines(file);
                list.clear();
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.endsWith(",")) {
                        trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
                    }
                    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                        list.add(trimmed);
                    } else if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                        list.add(trimmed.substring(1, trimmed.length() - 1));
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed reading " + file + ": " + e.getMessage());
            }
        }
    }

    private synchronized void saveList(Path file, List<String> list) {
        try {
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < list.size(); i++) {
                sb.append("  ").append(list.get(i));
                if (i < list.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            Files.writeString(file, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed writing " + file + ": " + e.getMessage());
        }
    }

    // --- History Operations ---
    public synchronized void addHistory(String from, String to, double amount, double rate, double convertedAmount) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = Instant.now().toString();
        String json = String.format(Locale.US,
            "{\"id\":\"%s\",\"timestamp\":\"%s\",\"from\":\"%s\",\"to\":\"%s\",\"amount\":%.2f,\"rate\":%.6f,\"convertedAmount\":%.2f}",
            id, timestamp, from, to, amount, rate, convertedAmount
        );
        historyList.add(0, json); // newest first
        if (historyList.size() > 100) historyList.remove(historyList.size() - 1);
        saveList(historyFile, historyList);
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(historyList);
    }

    public synchronized boolean deleteHistoryItem(String id) {
        boolean removed = historyList.removeIf(item -> item.contains("\"id\":\"" + id + "\""));
        if (removed) saveList(historyFile, historyList);
        return removed;
    }

    public synchronized void clearHistory() {
        historyList.clear();
        saveList(historyFile, historyList);
    }

    // --- Rate Alert Operations ---
    public synchronized String addAlert(String from, String to, double targetRate, String condition) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = Instant.now().toString();
        String json = String.format(Locale.US,
            "{\"id\":\"%s\",\"from\":\"%s\",\"to\":\"%s\",\"targetRate\":%.4f,\"condition\":\"%s\",\"active\":true,\"createdAt\":\"%s\"}",
            id, from.toUpperCase(), to.toUpperCase(), targetRate, condition.toLowerCase(), timestamp
        );
        alertsList.add(0, json);
        saveList(alertsFile, alertsList);
        return id;
    }

    public List<String> getAlerts() {
        return Collections.unmodifiableList(alertsList);
    }

    public synchronized boolean deleteAlert(String id) {
        boolean removed = alertsList.removeIf(item -> item.contains("\"id\":\"" + id + "\""));
        if (removed) saveList(alertsFile, alertsList);
        return removed;
    }

    // --- Notification Operations ---
    public synchronized void addNotification(String title, String message, String type) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = Instant.now().toString();
        String json = String.format(
            "{\"id\":\"%s\",\"title\":\"%s\",\"message\":\"%s\",\"type\":\"%s\",\"timestamp\":\"%s\",\"read\":false}",
            id, escapeJson(title), escapeJson(message), escapeJson(type), timestamp
        );
        notificationsList.add(0, json);
        if (notificationsList.size() > 50) notificationsList.remove(notificationsList.size() - 1);
        saveList(notificationsFile, notificationsList);
    }

    public List<String> getNotifications() {
        return Collections.unmodifiableList(notificationsList);
    }

    public synchronized void clearNotifications() {
        notificationsList.clear();
        saveList(notificationsFile, notificationsList);
    }

    // --- Favorites Operations ---
    public synchronized void toggleFavorite(String code) {
        String upper = code.toUpperCase();
        if (favoritesList.contains(upper)) {
            favoritesList.remove(upper);
        } else {
            favoritesList.add(upper);
        }
        saveFavorites();
    }

    public List<String> getFavorites() {
        return Collections.unmodifiableList(favoritesList);
    }

    private synchronized void saveFavorites() {
        try {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < favoritesList.size(); i++) {
                sb.append("\"").append(favoritesList.get(i)).append("\"");
                if (i < favoritesList.size() - 1) sb.append(",");
            }
            sb.append("]");
            Files.writeString(favoritesFile, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed saving favorites: " + e.getMessage());
        }
    }

    // --- Settings Operations ---
    public String getSettings() {
        return userSettingsJson;
    }

    public synchronized void saveSettings(String newSettingsJson) {
        this.userSettingsJson = newSettingsJson;
        try {
            Files.writeString(settingsFile, newSettingsJson, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed saving settings: " + e.getMessage());
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
