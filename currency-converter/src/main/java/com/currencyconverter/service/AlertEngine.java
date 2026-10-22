package com.currencyconverter.service;

import com.currencyconverter.store.AlertAndHistoryStore;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlertEngine {

    private final RateFetcherService rateFetcherService;
    private final AlertAndHistoryStore store;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Set<String> triggeredAlertIds = Collections.synchronizedSet(new HashSet<>());

    public AlertEngine(RateFetcherService rateFetcherService, AlertAndHistoryStore store) {
        this.rateFetcherService = rateFetcherService;
        this.store = store;
    }

    public void start() {
        // Schedule periodic check every 60 seconds
        scheduler.scheduleAtFixedRate(this::checkAlertsAndRates, 10, 60, TimeUnit.SECONDS);
        System.out.println("[AlertEngine] Background rate alert monitoring engine started.");
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void checkAlertsAndRates() {
        try {
            // First refresh rates
            boolean updated = rateFetcherService.fetchLatestRates();
            if (updated) {
                store.addNotification("Exchange Rates Updated", "Live rates refreshed automatically.", "info");
            }

            List<String> alerts = store.getAlerts();
            for (String alertJson : alerts) {
                processAlertJson(alertJson);
            }
        } catch (Exception e) {
            System.err.println("[AlertEngine] Error evaluating rate alerts: " + e.getMessage());
        }
    }

    private void processAlertJson(String json) {
        try {
            String id = extractString(json, "id");
            if (id == null || triggeredAlertIds.contains(id)) return;

            String from = extractString(json, "from");
            String to = extractString(json, "to");
            String condition = extractString(json, "condition");
            double targetRate = extractDouble(json, "targetRate");

            if (from == null || to == null || condition == null || targetRate == 0) return;

            double currentRate = rateFetcherService.getCrossRate(from, to);

            boolean triggered = false;
            if ("above".equalsIgnoreCase(condition) && currentRate >= targetRate) {
                triggered = true;
            } else if ("below".equalsIgnoreCase(condition) && currentRate <= targetRate) {
                triggered = true;
            }

            if (triggered) {
                triggeredAlertIds.add(id);
                String title = "🔔 Rate Alert Triggered!";
                String msg = String.format(Locale.US,
                    "Target condition met for %s/%s: Current rate %.4f is %s target %.4f.",
                    from, to, currentRate, condition, targetRate
                );
                store.addNotification(title, msg, "alert");
                System.out.println("[AlertEngine] " + title + " - " + msg);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private String extractString(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\":\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private double extractDouble(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\":\\s*([0-9.]+)");
        Matcher m = p.matcher(json);
        return m.find() ? Double.parseDouble(m.group(1)) : 0.0;
    }
}
