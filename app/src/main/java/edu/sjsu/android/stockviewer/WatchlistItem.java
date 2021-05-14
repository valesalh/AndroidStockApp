package edu.sjsu.android.stockviewer;

public class WatchlistItem {

    private String symbol;
    private int type;

    public static final int TYPE_STOCK = 0;
    public static final int TYPE_CRYPTO = 1;

    public WatchlistItem(String symbol, int type) {
        this.symbol = symbol;
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "WatchlistItem{" +
                "symbol='" + symbol + '\'' +
                ", type=" + type +
                '}';
    }
}
