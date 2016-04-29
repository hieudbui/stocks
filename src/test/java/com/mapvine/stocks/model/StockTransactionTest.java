package com.mapvine.stocks.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class StockTransactionTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void testHappyPathConstructor() {
        Stock stock = new Stock("GOOG", "", "1234");
        int numberOfShares = 1;
        assertEquals(numberOfShares, new StockTransaction(stock, numberOfShares).getNumberOfShares());
        assertSame(stock, new StockTransaction(stock, numberOfShares).getStock());
    }

    @Test
    public void testNullStockConstructor() {
        expected.expect(IllegalArgumentException.class);
        new StockTransaction(null, 1);
    }

    @Test
    public void testNumberOfSharesEqualsZeroConstructor() {
        assertEquals(0, new StockTransaction(new Stock("GOOG", "", "1234"), 0).getNumberOfShares());
    }

    @Test
    public void testNumberOfSharesLessThanZeroConstructor() {
        expected.expect(IllegalArgumentException.class);
        new StockTransaction(new Stock("GOOG", "", "1234"), -1);
    }

    @Test
    public void testGetValue() {
        final StockTransaction stockTransaction = new StockTransaction(new Stock("GOOG", "", "10.111111"), 100);
        assertEquals(1011.1111, stockTransaction.getValue().doubleValue(), 0);

    }
}