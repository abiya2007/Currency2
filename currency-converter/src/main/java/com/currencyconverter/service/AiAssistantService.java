package com.currencyconverter.service;

import com.currencyconverter.model.CurrencyData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiAssistantService {

    private final RateFetcherService rateFetcherService;

    public AiAssistantService(RateFetcherService rateFetcherService) {
        this.rateFetcherService = rateFetcherService;
    }

    public String generateResponse(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "Hello! I am your AI Currency Assistant 🤖. Ask me anything about exchange rates, bank fees, conversion advice, or travel money tips!";
        }

        String lower = prompt.toLowerCase();

        // 1. Fee / Calculation Question
        if (lower.contains("fee") || lower.contains("markup") || lower.contains("bank") || lower.contains("card")) {
            return handleFeeQuestion(prompt, lower);
        }

        // 2. Best time / trend advice
        if (lower.contains("best time") || lower.contains("should i convert") || lower.contains("trend") || lower.contains("forecast") || lower.contains("good time")) {
            return handleTrendAdvice(prompt, lower);
        }

        // 3. Travel money tips
        if (lower.contains("travel") || lower.contains("trip") || lower.contains("japan") || lower.contains("europe") || lower.contains("india") || lower.contains("dubai") || lower.contains("card vs cash")) {
            return handleTravelAdvice(prompt, lower);
        }

        // 4. Specific pair check (e.g. USD to INR)
        Pattern pairPattern = Pattern.compile("([a-z]{3})\\s*(to|in|->|for)\\s*([a-z]{3})");
        Matcher pairMatcher = pairPattern.matcher(lower);
        if (pairMatcher.find()) {
            String from = pairMatcher.group(1).toUpperCase();
            String to = pairMatcher.group(3).toUpperCase();
            double rate = rateFetcherService.getCrossRate(from, to);
            CurrencyData.CurrencyInfo fromInfo = CurrencyData.getCurrency(from);
            CurrencyData.CurrencyInfo toInfo = CurrencyData.getCurrency(to);

            return String.format(Locale.US,
                "🤖 **Live Rate Alert**: 1 %s (%s %s) = **%.4f %s** (%s %s).\n\n" +
                "💡 **AI Tip**: Currently, %s to %s is holding stable. If you are making a large transfer (> $1,000), consider checking bank conversion fees (typically 1.5%% - 3.5%%). Using a multi-currency travel card can save up to 2.5%% in foreign exchange markups!",
                from, fromInfo.getFlag(), fromInfo.getName(), rate, to, toInfo.getFlag(), toInfo.getName(),
                from, to
            );
        }

        // Default General AI response
        return "🤖 **AI FX Assistant**: I can help you analyze exchange rates, calculate bank conversion fees, compare currencies, and optimize travel expenses! Try asking:\n" +
               "• *\"What is the rate from USD to INR with a 2% bank fee?\"*\n" +
               "• *\"Is it a good time to exchange EUR to USD?\"*\n" +
               "• *\"What are the best money tips for traveling to Japan?\"*";
    }

    private String handleFeeQuestion(String prompt, String lower) {
        double amount = 1000;
        double feePct = 2.5;

        // Try extracting amount
        Pattern amtPat = Pattern.compile("([0-9,.]+)\\s*([a-z]{3})?");
        Matcher amtMat = amtPat.matcher(prompt);
        if (amtMat.find()) {
            try {
                amount = Double.parseDouble(amtMat.group(1).replace(",", ""));
            } catch (Exception e) {}
        }

        // Try extracting fee percentage
        Pattern feePat = Pattern.compile("([0-9.]+)\\s*%");
        Matcher feeMat = feePat.matcher(prompt);
        if (feeMat.find()) {
            try {
                feePct = Double.parseDouble(feeMat.group(1));
            } catch (Exception e) {}
        }

        double feeAmount = amount * (feePct / 100.0);
        double netAmount = amount - feeAmount;

        return String.format(Locale.US,
            "🤖 **Fee Breakdown Analysis**:\n" +
            "• **Gross Amount**: $%.2f\n" +
            "• **Bank/Card Fee (%s%%)**: -$%.2f\n" +
            "• **Net Exchanged Amount**: $%.2f\n\n" +
            "⚠️ **AI Recommendation**: Standard high-street banks charge between 2.5%% to 4%% in hidden exchange markups. Using zero-markup cards (like Wise or Revolut) can save you approximately $%.2f on this transaction!",
            amount, feePct, feeAmount, netAmount, feeAmount
        );
    }

    private String handleTrendAdvice(String prompt, String lower) {
        return "🤖 **AI Trend Analysis & Forecast**:\n\n" +
               "📈 **Market Sentiment**: Major currency pairs (USD, EUR, GBP, INR, JPY) are experiencing moderate volatility due to global central bank interest rate policies.\n\n" +
               "💡 **Smart Converting Advice**:\n" +
               "1. **Dollar Strengths (USD)**: If converting USD into emerging market currencies (e.g. INR, PKR, BDT), current rates are near favorable highs.\n" +
               "2. **Dollar Cost Averaging**: If you have a large sum to convert, split your transaction into 2-3 installments over a week to hedge against short-term dips.\n" +
               "3. **Set Rate Alerts**: Use our **Rate Alert** tab to get notified immediately when your target threshold is hit!";
    }

    private String handleTravelAdvice(String prompt, String lower) {
        return "🤖 **AI Travel Money Advisor**:\n\n" +
               "✈️ **Top 4 Smart Money Rules for International Travelers**:\n" +
               "1. **Always Pay in Local Currency**: When paying at card terminals abroad, always choose *Pay in Local Currency* (e.g. EUR in Europe, JPY in Japan). Never choose Dynamic Currency Conversion (DCC) in your home currency, which incurs 4-7% hidden fees!\n" +
               "2. **Use Multi-Currency Cards**: Carry a prepaid travel card or credit card with 0% Foreign Transaction Fee.\n" +
               "3. **Emergency Cash**: Keep $100-$200 equivalent in crisp cash for local taxis or markets.\n" +
               "4. **Track Daily Spending**: Use our **Travel Expense Tracker** tab to log your meals, transport, and hotels in real-time!";
    }
}
