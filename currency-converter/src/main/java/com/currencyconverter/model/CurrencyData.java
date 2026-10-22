package com.currencyconverter.model;

import java.util.*;

public class CurrencyData {

    public static class CurrencyInfo {
        private final String code;
        private final String name;
        private final String symbol;
        private final String flag;
        private final String country;

        public CurrencyInfo(String code, String name, String symbol, String flag, String country) {
            this.code = code;
            this.name = name;
            this.symbol = symbol;
            this.flag = flag;
            this.country = country;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getSymbol() { return symbol; }
        public String getFlag() { return flag; }
        public String getCountry() { return country; }

        public String toJson() {
            return String.format(
                "{\"code\":\"%s\",\"name\":\"%s\",\"symbol\":\"%s\",\"flag\":\"%s\",\"country\":\"%s\"}",
                escapeJson(code), escapeJson(name), escapeJson(symbol), escapeJson(flag), escapeJson(country)
            );
        }

        private String escapeJson(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    private static final Map<String, CurrencyInfo> CURRENCIES = new LinkedHashMap<>();

    static {
        add("USD", "US Dollar", "$", "🇺🇸", "United States");
        add("EUR", "Euro", "€", "🇪🇺", "European Union");
        add("GBP", "British Pound", "£", "🇬🇧", "United Kingdom");
        add("INR", "Indian Rupee", "₹", "🇮🇳", "India");
        add("AED", "UAE Dirham", "د.إ", "🇦🇪", "United Arab Emirates");
        add("SAR", "Saudi Riyal", "﷼", "🇸🇦", "Saudi Arabia");
        add("JPY", "Japanese Yen", "¥", "🇯🇵", "Japan");
        add("CAD", "Canadian Dollar", "CA$", "🇨🇦", "Canada");
        add("AUD", "Australian Dollar", "A$", "🇦🇺", "Australia");
        add("CHF", "Swiss Franc", "CHF", "🇨🇭", "Switzerland");
        add("CNY", "Chinese Yuan", "¥", "🇨🇳", "China");
        add("SGD", "Singapore Dollar", "S$", "🇸🇬", "Singapore");
        add("NZD", "New Zealand Dollar", "NZ$", "🇳🇿", "New Zealand");
        add("HKD", "Hong Kong Dollar", "HK$", "🇭🇰", "Hong Kong");
        add("SEK", "Swedish Krona", "kr", "🇸🇪", "Sweden");
        add("KRW", "South Korean Won", "₩", "🇰🇷", "South Korea");
        add("BRL", "Brazilian Real", "R$", "🇧🇷", "Brazil");
        add("RUB", "Russian Ruble", "₽", "🇷🇺", "Russia");
        add("ZAR", "South African Rand", "R", "🇿🇦", "South Africa");
        add("MXN", "Mexican Peso", "Mex$", "🇲🇽", "Mexico");
        add("TRY", "Turkish Lira", "₺", "🇹🇷", "Turkey");
        add("KWD", "Kuwaiti Dinar", "د.ك", "🇰🇼", "Kuwait");
        add("BHD", "Bahraini Dinar", "د.ب", "🇧🇭", "Bahrain");
        add("OMR", "Omani Rial", "ر.ع.", "🇴🇲", "Oman");
        add("QAR", "Qatari Riyal", "ر.ق", "🇶🇦", "Qatar");
        add("MYR", "Malaysian Ringgit", "RM", "🇲🇾", "Malaysia");
        add("THB", "Thai Baht", "฿", "🇹🇭", "Thailand");
        add("IDR", "Indonesian Rupiah", "Rp", "🇮🇩", "Indonesia");
        add("PHP", "Philippine Peso", "₱", "🇵🇭", "Philippines");
        add("VND", "Vietnamese Dong", "₫", "🇻🇳", "Vietnam");
        add("EGP", "Egyptian Pound", "E£", "🇪🇬", "Egypt");
        add("PKR", "Pakistani Rupee", "₨", "🇵🇰", "Pakistan");
        add("BDT", "Bangladeshi Taka", "৳", "🇧🇩", "Bangladesh");
        add("LKR", "Sri Lankan Rupee", "Rs", "🇱🇰", "Sri Lanka");
        add("NPR", "Nepalese Rupee", "Rs", "🇳🇵", "Nepal");
        add("NOK", "Norwegian Krone", "kr", "🇳🇴", "Norway");
        add("DKK", "Danish Krone", "kr", "🇩🇰", "Denmark");
        add("PLN", "Polish Zloty", "zł", "🇵🇱", "Poland");
        add("CZK", "Czech Koruna", "Kč", "🇨🇿", "Czech Republic");
        add("HUF", "Hungarian Forint", "Ft", "🇭🇺", "Hungary");
        add("ILS", "Israeli New Shekel", "₪", "🇮🇱", "Israel");
        add("CLP", "Chilean Peso", "CLP$", "🇨🇱", "Chile");
        add("COP", "Colombian Peso", "COL$", "🇨🇴", "Colombia");
        add("PEN", "Peruvian Sol", "S/", "🇵🇪", "Peru");
        add("ARS", "Argentine Peso", "$", "🇦🇷", "Argentina");
        add("JOD", "Jordanian Dinar", "JD", "🇯🇴", "Jordan");
        add("IQD", "Iraqi Dinar", "ع.د", "🇮🇶", "Iraq");
        add("LBP", "Lebanese Pound", "L£", "🇱🇧", "Lebanon");
        add("TWD", "New Taiwan Dollar", "NT$", "🇹🇼", "Taiwan");
        add("NGN", "Nigerian Naira", "₦", "🇳🇬", "Nigeria");
        add("KES", "Kenyan Shilling", "KSh", "🇰🇪", "Kenya");
        add("GHS", "Ghanaian Cedi", "GH₵", "🇬🇭", "Ghana");
        add("MAD", "Moroccan Dirham", "MAD", "🇲🇦", "Morocco");
        add("DZD", "Algerian Dinar", "DA", "🇩🇿", "Algeria");
        add("TND", "Tunisian Dinar", "DT", "🇹🇳", "Tunisia");
        add("UGX", "Ugandan Shilling", "USh", "🇺🇬", "Uganda");
        add("TZS", "Tanzanian Shilling", "TSh", "🇹🇿", "Tanzania");
        add("ETB", "Ethiopian Birr", "Br", "🇪🇹", "Ethiopia");
        add("RON", "Romanian Leu", "lei", "🇷🇴", "Romania");
        add("BGN", "Bulgarian Lev", "лв", "🇧🇬", "Bulgaria");
        add("HRK", "Croatian Kuna", "kn", "🇭🇷", "Croatia");
        add("ISK", "Icelandic Krona", "kr", "🇮🇸", "Iceland");
        add("RSD", "Serbian Dinar", "din.", "🇷🇸", "Serbia");
        add("UAH", "Ukrainian Hryvnia", "₴", "🇺🇦", "Ukraine");
        add("GEL", "Georgian Lari", "₾", "🇬🇪", "Georgia");
        add("KZT", "Kazakhstani Tenge", "₸", "🇰🇿", "Kazakhstan");
        add("UZS", "Uzbekistani Som", "so'm", "🇺🇿", "Uzbekistan");
        add("AZN", "Azerbaijani Manat", "₼", "🇦🇿", "Azerbaijan");
        add("AMD", "Armenian Dram", "֏", "🇦🇲", "Armenia");
        add("MUR", "Mauritian Rupee", "₨", "🇲🇺", "Mauritius");
        add("CRC", "Costa Rican Colon", "₡", "🇨🇷", "Costa Rica");
        add("DOP", "Dominican Peso", "RD$", "🇩🇴", "Dominican Republic");
        add("GTQ", "Guatemalan Quetzal", "Q", "🇬🇹", "Guatemala");
        add("HNL", "Honduran Lempira", "L", "🇭🇳", "Honduras");
        add("NIO", "Nicaraguan Cordoba", "C$", "🇳🇮", "Nicaragua");
        add("PAB", "Panamanian Balboa", "B/.", "🇵🇦", "Panama");
        add("PYG", "Paraguayan Guarani", "₲", "🇵🇾", "Paraguay");
        add("UYU", "Uruguayan Peso", "$U", "🇺🇾", "Uruguay");
        add("BOB", "Bolivian Boliviano", "Bs.", "🇧🇴", "Bolivia");
        add("JMD", "Jamaican Dollar", "J$", "🇯🇲", "Jamaica");
        add("TTD", "Trinidad and Tobago Dollar", "TT$", "🇹🇹", "Trinidad & Tobago");
        add("BBD", "Barbadian Dollar", "Bds$", "🇧🇧", "Barbados");
        add("BZD", "Belize Dollar", "BZ$", "🇧🇿", "Belize");
        add("FJD", "Fijian Dollar", "FJ$", "🇫🇯", "Fiji");
        add("PGK", "Papua New Guinean Kina", "K", "🇵🇬", "Papua New Guinea");
        add("WST", "Samoan Tala", "WS$", "🇼🇸", "Samoa");
        add("TOP", "Tongan Pa'anga", "T$", "🇹🇴", "Tonga");
        add("VUV", "Vanuatu Vatu", "VT", "🇻🇺", "Vanuatu");
        add("ALL", "Albanian Lek", "L", "🇦🇱", "Albania");
        add("BAM", "Bosnia-Herzegovina Mark", "KM", "🇧🇦", "Bosnia & Herzegovina");
        add("MKD", "Macedonian Denar", "ден", "🇲🇰", "North Macedonia");
        add("MDL", "Moldovan Leu", "L", "🇲🇩", "Moldova");
        add("MNT", "Mongolian Tugrik", "₮", "🇲🇳", "Mongolia");
        add("LAK", "Lao Kip", "₭", "🇱🇦", "Laos");
        add("MMK", "Myanmar Kyat", "K", "🇲🇲", "Myanmar");
        add("KHR", "Cambodian Riel", "៛", "🇰🇭", "Cambodia");
        add("BND", "Brunei Dollar", "B$", "🇧🇳", "Brunei");
        add("MVR", "Maldivian Rufiyaa", "Rf", "🇲🇻", "Maldives");
        add("AFN", "Afghan Afghani", "؋", "🇦🇫", "Afghanistan");
        add("TJS", "Tajikistani Somoni", "SM", "🇹🇯", "Tajikistan");
        add("KGS", "Kyrgystani Som", "сом", "🇰🇬", "Kyrgyzstan");
        add("TMT", "Turkmenistani Manat", "TMT", "🇹🇲", "Turkmenistan");
        add("IRR", "Iranian Rial", "﷼", "🇮🇷", "Iran");
        add("SYP", "Syrian Pound", "LS", "🇸🇾", "Syria");
        add("YER", "Yemeni Rial", "﷼", "🇾🇪", "Yemen");
        add("LYD", "Libyan Dinar", "LD", "🇱🇾", "Libya");
        add("SDG", "Sudanese Pound", "SDG", "🇸🇩", "Sudan");
        add("SOS", "Somali Shilling", "Sh.So.", "🇸🇴", "Somalia");
        add("MWK", "Malawian Kwacha", "MK", "🇲🇼", "Malawi");
        add("ZMW", "Zambian Kwacha", "ZK", "🇿🇲", "Zambia");
        add("BWP", "Botswana Pula", "P", "🇧🇼", "Botswana");
        add("NAD", "Namibian Dollar", "N$", "🇳🇦", "Namibia");
        add("SZL", "Swazi Lilangeni", "L", "🇸🇿", "Eswatini");
        add("LSL", "Lesotho Loti", "L", "🇱🇸", "Lesotho");
        add("MGA", "Malagasy Ariary", "Ar", "🇲🇬", "Madagascar");
        add("SCR", "Seychellois Rupee", "SR", "🇸🇨", "Seychelles");
        add("CVE", "Cape Verdean Escudo", "Esc", "🇨🇻", "Cape Verde");
        add("GMD", "Gambian Dalasi", "D", "🇬🇲", "Gambia");
        add("SLL", "Sierra Leonean Leone", "Le", "🇸🇱", "Sierra Leone");
        add("LRD", "Liberian Dollar", "L$", "🇱🇷", "Liberia");
        add("RWF", "Rwandan Franc", "FRw", "🇷🇼", "Rwanda");
        add("BIF", "Burundian Franc", "FBu", "🇧🇮", "Burundi");
        add("DJF", "Djiboutian Franc", "Fdj", "🇩🇯", "Djibouti");
        add("GNF", "Guinean Franc", "FG", "🇬🇳", "Guinea");
        add("KMF", "Comorian Franc", "CF", "🇰🇲", "Comoros");
        add("MZN", "Mozambican Metical", "MT", "🇲🇿", "Mozambique");
        add("AOA", "Angolan Kwanza", "Kz", "🇦🇴", "Angola");
        add("CDF", "Congolese Franc", "FC", "🇨🇩", "DR Congo");
        add("XOF", "West African CFA Franc", "CFA", "🌍", "West Africa");
        add("XAF", "Central African CFA Franc", "FCFA", "🌍", "Central Africa");
        add("XPF", "CFP Franc", "₣", "🇵🇫", "French Polynesia");
        add("XCD", "East Caribbean Dollar", "EC$", "🇦🇮", "East Caribbean");
        add("BSD", "Bahamian Dollar", "B$", "🇧🇸", "Bahamas");
        add("CUP", "Cuban Peso", "$MN", "🇨🇺", "Cuba");
        add("HTG", "Haitian Gourde", "G", "🇭🇹", "Haiti");
        add("AWG", "Aruban Florin", "Afl.", "🇦🇼", "Aruba");
        add("ANG", "Netherlands Antillean Guilder", "NAƒ", "🇨🇼", "Curacao");
        add("SRD", "Surinamese Dollar", "Sr$", "🇸🇷", "Suriname");
        add("GYD", "Guyanaese Dollar", "G$", "🇬🇾", "Guyana");
        add("KYD", "Cayman Islands Dollar", "CI$", "🇰🇾", "Cayman Islands");
        add("BMD", "Bermudian Dollar", "BD$", "🇧🇲", "Bermuda");
    }

    private static void add(String code, String name, String symbol, String flag, String country) {
        CURRENCIES.put(code, new CurrencyInfo(code, name, symbol, flag, country));
    }

    public static Map<String, CurrencyInfo> getAllCurrencies() {
        return Collections.unmodifiableMap(CURRENCIES);
    }

    public static CurrencyInfo getCurrency(String code) {
        if (code == null) return null;
        CurrencyInfo info = CURRENCIES.get(code.toUpperCase());
        if (info == null) {
            return new CurrencyInfo(code.toUpperCase(), code.toUpperCase(), code.toUpperCase(), "🏳️", "Global");
        }
        return info;
    }

    public static String getAllCurrenciesJson() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, CurrencyInfo> entry : CURRENCIES.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue().toJson());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
