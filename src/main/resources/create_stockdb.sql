-- Create the trading schema
CREATE SCHEMA IF NOT EXISTS trading;

-- Create the "stocks" table inside the "trading" schema
CREATE TABLE trading.stocks (
                                id SERIAL PRIMARY KEY,
                                ticker VARCHAR(10) NOT NULL UNIQUE,
                                company_name VARCHAR(255) NOT NULL,
                                stock_price DECIMAL(10, 2) NOT NULL,
                                description TEXT
);

-- Optional: Insert sample data
INSERT INTO trading.stocks (ticker, company_name, stock_price, description)
VALUES
    ('AAPL', 'Apple Inc.', 190.25, 'Technology company specializing in consumer electronics.'),
    ('GOOGL', 'Alphabet Inc.', 135.80, 'Parent company of Google and related businesses.'),
    ('AMZN', 'Amazon.com Inc.', 145.12, 'E-commerce and cloud computing leader.'),
    ('MSFT', 'Microsoft Corp.', 365.50, 'Developer of software and cloud solutions.');