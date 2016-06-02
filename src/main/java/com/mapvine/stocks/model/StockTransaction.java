package com.mapvine.stocks.model;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;

public class StockTransaction {
    private Stock stock;
    private int numberOfShares;

    public StockTransaction(Stock stock, int numberOfShares) {
        Preconditions.checkArgument(stock != null, "stock is required!");
        Preconditions.checkArgument(numberOfShares >= 0, "numberOfShares must be greater than or equals to zero!");
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
