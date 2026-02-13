package com.example.stockapp.service;

import com.example.stockapp.entity.Stock;

import java.util.List;
import java.util.Optional;

public interface StockService {
    Stock createStock(Stock stock);

    List<Stock> getAllStocks();

    Optional<Stock> getStockByTicker(String ticker);

    Stock updateStock(String ticker, Stock stock);

    void deleteStock(String ticker);
}
