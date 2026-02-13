package com.example.stockapp.controller;

import com.example.stockapp.entity.Stock;
import com.example.stockapp.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/basic")
public class BasicController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello World");
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    // Dependency injection via constructor (immutable field)
    private final StockService service;

    /**
     * Constructor-based dependency injection
     *
     * @param service StockService implementation for business logic
     */
    public BasicController(StockService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Stock> createStock(@RequestBody Stock stock) {
        Stock created = service.createStock(stock);
        // Return 201 Created with Location header pointing to new resource
        return ResponseEntity.created(URI.create("/api/stocks/" + created.getTicker())).body(created);
    }
}