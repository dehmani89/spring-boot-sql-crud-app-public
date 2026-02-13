package com.example.stockapp.service;

import com.example.stockapp.entity.Stock;
import com.example.stockapp.exception.ResourceNotFoundException;
import com.example.stockapp.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StockServiceImpl implements StockService {
    private final StockRepository repo;

    public StockServiceImpl(StockRepository repo) {
        this.repo = repo;
    }

    public Stock createStock(Stock stock) {
        return repo.save(stock);
    }

    public List<Stock> getAllStocks() {
        return repo.findAll();
    }

    public Optional<Stock> getStockByTicker(String ticker) {
        return repo.findById(ticker.toUpperCase());
    }

    public Stock updateStock(String ticker, Stock stock) {
        Stock existing = repo.findById(ticker.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + ticker));
        existing.setCompanyName(stock.getCompanyName());
        existing.setPrice(stock.getPrice());
        existing.setDescription(stock.getDescription());
        return repo.save(existing);
    }

    public void deleteStock(String ticker) {
        Stock existing = repo.findById(ticker.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + ticker));
        repo.delete(existing);
    }
}
