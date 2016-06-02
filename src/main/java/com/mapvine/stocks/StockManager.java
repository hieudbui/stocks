package com.mapvine.stocks;

import com.google.common.base.Preconditions;
import com.mapvine.stocks.model.Stock;
import com.mapvine.stocks.model.StockTransaction;
import org.apache.commons.lang3.mutable.MutableInt;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class StockManager {

    /**
     * TODO
     * Needs more analysis on whether using a ConcurrentLinkedDeque is the right choice
     */
    private Collection<StockTransaction> stockPurchases = new ConcurrentLinkedDeque<>();
    private Collection<StockTransaction> stockSales = new ConcurrentLinkedDeque<>();


    /**
     * Get the list of stocks currently under management by its ticker symbol.
     */
    public List<Stock> findByTicker(final String ticker) {
        return findStockUnderManagement(ticker).stream()
                .map(stockPurchase -> stockPurchase.getStock())
                .collect(Collectors.toList());
    }

    /**
     * For a given ticker symbol, add up the total stock values under management and return.
     */
    public BigDecimal getValueUnderManagerByTicker(final String ticker) {
        final BigDecimal value = sumValue(findStockUnderManagement(ticker));
        return value.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : value.setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * For a given ticker symbol, get the number of stocks under management.
     */
    public int numberOfStocksByTicker(final String ticker) {
        return sumShares(findStockUnderManagement(ticker));
    }

    /**
     * Add a new stock to be managed by our system (here 'buy' is equal to 'add').
     *
     * @param stock          - The {@link com.mapvine.stocks.model.Stock} to buy
     * @param numberOfShares - The number of shares to purchase
     * @throws java.lang.IllegalArgumentException if numberOfShares is <= 0
     */
    public void buyStock(final Stock stock, final int numberOfShares) {
        Preconditions.checkArgument(numberOfShares > 0, "number of shares must be greater than 0");
        stockPurchases.add(new StockTransaction(stock, numberOfShares));
    }

    /**
     * For a given ticker, sell the stock. The stock sold should be returned before being removed from management.
     * <p>
     * This method will attempt to sell stock starting with the lowest price.
     *
     * @param ticker         - The stock ticker to sell
     * @param numberOfShares - The number of shares to purchase
     * @param sharePrice     - The price we are selling numberOfShares
     * @return A list of the stock prices that were sold sorted from most to least expensive
     * @throws java.lang.IllegalArgumentException if numberOfShares is <= 0
     */
    public Optional<Set<BigDecimal>> sellStock(final String ticker, final int numberOfShares, final BigDecimal sharePrice) {
        Preconditions.checkArgument(numberOfShares > 0, "number of shares must be greater than 0");
        if (findNumberOfSharesPurchased(ticker) < numberOfShares) {
            return Optional.empty();
        }
        stockSales.add(new StockTransaction(new Stock(ticker, "", sharePrice), numberOfShares));
        final List<BigDecimal> salePrices = stockSales.stream().map(st -> st.getValue()).collect(Collectors.toList());
        salePrices.sort((a, b) -> a.compareTo(b));
        return Optional.of(new TreeSet(salePrices));
    }

    /**
     * For a given stock ticker, get the P&L.
     *
     * @return The total profit made so far or an empty option if the stock is not under management
     */
    public Optional<BigDecimal> getProfitForStockByTicker(final String ticker) {
        if (findNumberOfSharesPurchased(ticker) <= 0) {
            return Optional.empty();
        }
        return Optional.of(getAverageStockSoldPrice(ticker).subtract(getAverageStockPurchasedPrice(ticker)));
    }

    /**
     * Ordered by cheapest price first
     */
    private List<StockTransaction> findStockPurchases(final String ticker) {
        final List<StockTransaction> cheapestFirstStockTransactions = stockPurchases.stream()
                .filter(stockPurchase -> stockPurchase.getStock().getTicker().equalsIgnoreCase(ticker.trim()))
                .collect(Collectors.toList());
        cheapestFirstStockTransactions.sort((a, b) -> a.getStock().getPrice().compareTo(b.getStock().getPrice()));
        return cheapestFirstStockTransactions;
    }

    private List<StockTransaction> findStockSales(final String ticker) {
        return stockSales.stream()
                .filter(stockPurchase -> stockPurchase.getStock().getTicker().equalsIgnoreCase(ticker.trim()))
                .collect(Collectors.toList());
    }

    /**
     * TODO
     * This needs more thorough testing
     * Also I don't like the imperative style of having side effect using mutable int
     */
    private List<StockTransaction> findStockUnderManagement(final String ticker) {
        final List<StockTransaction> stockPurchases = findStockPurchases(ticker);

        final MutableInt numberOfSharesSold = new MutableInt(findNumberOfSharesSold(ticker));

        List<StockTransaction> stockUnderManagement = stockPurchases.stream().map(st -> {
            int numberOfSharesPurchased = st.getNumberOfShares();
            if (numberOfSharesSold.getValue() > 0) {
                int sharesDelta = numberOfSharesPurchased - numberOfSharesSold.getValue();
                if (sharesDelta < 0) {
                    numberOfSharesSold.subtract(numberOfSharesPurchased);
                    numberOfSharesPurchased = 0;
                } else {
                    numberOfSharesSold.subtract(sharesDelta);
                    numberOfSharesPurchased = sharesDelta;
                }
            }
            return new StockTransaction(st.getStock(), numberOfSharesPurchased);
        }).filter(st -> st.getNumberOfShares() > 0).collect(Collectors.toList());
        return stockUnderManagement;
    }

    private int findNumberOfSharesSold(String ticker) {
        return sumShares(findStockSales(ticker));
    }

    private int findNumberOfSharesPurchased(String ticker) {
        return sumShares(findStockPurchases(ticker));
    }

    private int sumShares(Collection<StockTransaction> stockTransactions) {
        return stockTransactions.stream()
                .map(sp -> sp.getNumberOfShares())
                .reduce(0, (a, b) -> a + b);
    }

    private BigDecimal getAverageStockPurchasedPrice(String ticker) {
        return averagePrice(sumValue(findStockPurchases(ticker)), findNumberOfSharesPurchased(ticker));
    }

    private BigDecimal getAverageStockSoldPrice(String ticker) {
        return averagePrice(sumValue(findStockSales(ticker)), findNumberOfSharesSold(ticker));
    }

    private BigDecimal sumValue(Collection<StockTransaction> stockTransactions) {
        return stockTransactions.stream().map(sp -> sp.getValue())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
    }

    private BigDecimal averagePrice(BigDecimal totalValue, int numberofShares) {
        return totalValue.divide(new BigDecimal(numberofShares));
    }
}
