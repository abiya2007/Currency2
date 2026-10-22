package com.currencyconverter.store;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpenseStore {

    private final Path dataDir;
    private final Path expensesFile;
    private final List<String> expensesList = new CopyOnWriteArrayList<>();

    public ExpenseStore() {
        this.dataDir = Paths.get("data");
        try {
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
        } catch (IOException e) {
            System.err.println("ExpenseStore dir error: " + e.getMessage());
        }
        this.expensesFile = dataDir.resolve("expenses.json");
        loadExpenses();
    }

    private void loadExpenses() {
        if (Files.exists(expensesFile)) {
            try {
                List<String> lines = Files.readAllLines(expensesFile);
                expensesList.clear();
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.endsWith(",")) {
                        trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
                    }
                    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                        expensesList.add(trimmed);
                    }
                }
            } catch (IOException e) {
                System.err.println("ExpenseStore load error: " + e.getMessage());
            }
        }
    }

    private synchronized void saveExpenses() {
        try {
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < expensesList.size(); i++) {
                sb.append("  ").append(expensesList.get(i));
                if (i < expensesList.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            Files.writeString(expensesFile, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("ExpenseStore save error: " + e.getMessage());
        }
    }

    public synchronized String addExpense(String title, String category, double amountTravelCurr, String travelCurr, double amountHomeCurr, String homeCurr) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = Instant.now().toString();
        String json = String.format(Locale.US,
            "{\"id\":\"%s\",\"title\":\"%s\",\"category\":\"%s\",\"amountTravel\":%.2f,\"travelCurr\":\"%s\",\"amountHome\":%.2f,\"homeCurr\":\"%s\",\"timestamp\":\"%s\"}",
            id, escapeJson(title), escapeJson(category), amountTravelCurr, travelCurr.toUpperCase(), amountHomeCurr, homeCurr.toUpperCase(), timestamp
        );
        expensesList.add(0, json);
        saveExpenses();
        return id;
    }

    public List<String> getExpenses() {
        return Collections.unmodifiableList(expensesList);
    }

    public synchronized boolean deleteExpense(String id) {
        boolean removed = expensesList.removeIf(item -> item.contains("\"id\":\"" + id + "\""));
        if (removed) saveExpenses();
        return removed;
    }

    public synchronized void clearExpenses() {
        expensesList.clear();
        saveExpenses();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
