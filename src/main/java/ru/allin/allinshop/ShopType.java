package ru.allin.allinshop;

public enum ShopType {
    SELL, BUY, BOTH;
    public static ShopType parse(String s) {
        return switch (s.toLowerCase()) {
            case "sell", "продажа" -> SELL;
            case "buy", "покупка" -> BUY;
            case "both", "обмен", "оба" -> BOTH;
            default -> null;
        };
    }
}
