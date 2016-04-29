package com.mapvine.stocks.model;

import java.math.BigDecimal;

public class StockTransaction {
    private Stock stock;
    private int numberOfShares;

    public StockTransaction(Stock stock, int numberOfShares) {
        if (stock == null) {
            throw new IllegalArgumentException("stock is required!");
        } else if (numberOfShares < 0) {
            throw new IllegalArgumentException("numberOfShares must be greater than or equals to zero!");
        }
        this.stock = stock;
        this.numberOfShares = numberOfShares;
    }

    public Stock getStock() {
        return stock;
    }

    public int getNumberOfShares() {
        return numberOfShares;
    }

    public BigDecimal getValue() {
        return new BigDecimal(numberOfShares).multiply(stock.getPrice()).setScale(4, BigDecimal.ROUND_HALF_UP);
    }
}
