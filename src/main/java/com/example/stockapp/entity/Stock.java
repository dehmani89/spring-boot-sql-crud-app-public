package com.example.stockapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "stocks", schema = "trading")
public class Stock {
    @Id
    @Column(name = "ticker", nullable = false, length = 10)
    private String ticker;
    @Column(name = "company_name", nullable = false)
    private String companyName;
    @Column(name = "stock_price", nullable = false)
    private BigDecimal price;
    @Column(name = "description", length = 1000)
    private String description;

    public Stock() {
    }

    public Stock(String ticker, String companyName, BigDecimal price, String description) {
        this.ticker = ticker;
        this.companyName = companyName;
        this.price = price;
        this.description = description;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
