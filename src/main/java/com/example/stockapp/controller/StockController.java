package com.example.stockapp.controller;

import com.example.stockapp.entity.Stock;
import com.example.stockapp.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST Controller for Stock Management Operations
 * 
 * Provides CRUD (Create, Read, Update, Delete) operations for stock data.
 * All endpoints require JWT authentication (configured in SecurityConfig).
 * CORS enabled for React frontend integration.
 * 
 * Base URL: /api/stocks
 * Authentication: JWT token required in Authorization header
 * Frontend: React app on localhost:3000
 */
@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class StockController {
    
    // Dependency injection via constructor (immutable field)
    private final StockService service;

    /**
     * Constructor-based dependency injection
     * 
     * @param service StockService implementation for business logic
     */
    public StockController(StockService service) {
        this.service = service;
    }

    /**
     * Create a new stock entry
     * 
     * POST /api/stocks
     * 
     * @param stock Stock object from request body (JSON)
     * @return ResponseEntity with created stock and 201 status + Location header
     */
    @PostMapping
    public ResponseEntity<Stock> createStock(@RequestBody Stock stock) {
        Stock created = service.createStock(stock);
        // Return 201 Created with Location header pointing to new resource
        return ResponseEntity.created(URI.create("/api/stocks/" + created.getTicker())).body(created);
    }

    /**
     * Retrieve all stocks
     * 
     * GET /api/stocks
     * 
     * @return ResponseEntity with list of all stocks and 200 status
     */
    @GetMapping
    public ResponseEntity<List<Stock>> getAllStocks() {
        return ResponseEntity.ok(service.getAllStocks());
    }

    /**
     * Retrieve a specific stock by ticker symbol
     * 
     * GET /api/stocks/{ticker}
     * 
     * @param ticker Stock ticker symbol from URL path
     * @return ResponseEntity with stock data and 200 status
     * @throws ResourceNotFoundException if stock not found (returns 404)
     */
    @GetMapping("/{ticker}")
    public ResponseEntity<Stock> getStock(@PathVariable String ticker) {
        return service.getStockByTicker(ticker)
                .map(ResponseEntity::ok)  // If found, return 200 OK
                .orElseThrow(() -> new com.example.stockapp.exception.ResourceNotFoundException("Stock not found: " + ticker));
    }

    /**
     * Update an existing stock
     * 
     * PUT /api/stocks/{ticker}
     * 
     * @param ticker Stock ticker symbol from URL path
     * @param stock Updated stock data from request body (JSON)
     * @return ResponseEntity with updated stock and 200 status
     */
    @PutMapping("/{ticker}")
    public ResponseEntity<Stock> updateStock(@PathVariable String ticker, @RequestBody Stock stock) {
        return ResponseEntity.ok(service.updateStock(ticker, stock));
    }

    /**
     * Delete a stock by ticker symbol
     * 
     * DELETE /api/stocks/{ticker}
     * 
     * @param ticker Stock ticker symbol from URL path
     * @return ResponseEntity with 204 No Content status (successful deletion)
     */
    @DeleteMapping("/{ticker}")
    public ResponseEntity<Void> deleteStock(@PathVariable String ticker) {
        service.deleteStock(ticker);
        // Return 204 No Content - successful deletion with no response body
        return ResponseEntity.noContent().build();
    }
}
