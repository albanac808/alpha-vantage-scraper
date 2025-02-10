package com.financialdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;


public class StockScraper {
    private final CloseableHttpClient httpClient;
    private final ObjectMapper jsonMapper;
    private final String apiKey;
    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private static final Logger logger = LoggerFactory.getLogger(StockScraper.class);

    public StockScraper(String apiKey) {
        this.httpClient = HttpClients.createDefault();
        this.jsonMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }

    public static void main(String[] args) {
        String apiKey = "Y20N8Z1XR53HMF2D";
        StockScraper scraper = new StockScraper(apiKey);
        try {
            scraper.fetchDailyStockData("IBM");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * @param symbol
     * @throws Exception
     */
    public void fetchDailyStockData(String symbol) throws Exception {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Stock symbol cannot be empty");
        }
    
        String url = String.format("%s?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s",
                BASE_URL, symbol, apiKey);
        
        HttpGet request = new HttpGet(url);
        logger.info("Fetching data for: {}", symbol);
        
        try {
            String response = httpClient.execute(request, httpResponse -> {
                int statusCode = httpResponse.getCode();
                if (statusCode != 200) {
                    throw new RuntimeException("API call failed with status: " + statusCode);
                }
                return new String(httpResponse.getEntity().getContent().readAllBytes());
            });
            if (response.contains("Error Message")) {
                throw new RuntimeException("API returned error: " + response);
            }
    
            // Get the most recent data point
            var jsonNode = jsonMapper.readTree(response);
            var timeSeriesData = jsonNode.get("Time Series (Daily)");
            var firstEntry = timeSeriesData.fields().next();
            var date = firstEntry.getKey();
            var data = firstEntry.getValue();
    
            StockData stockData = new StockData();
            stockData.date = date;
            stockData.open = Double.parseDouble(data.get("1. open").asText());
            stockData.high = Double.parseDouble(data.get("2. high").asText());
            stockData.low = Double.parseDouble(data.get("3. low").asText());
            stockData.close = Double.parseDouble(data.get("4. close").asText());
            stockData.volume = Long.parseLong(data.get("5. volume").asText());
    
            System.out.println("\nMost recent stock data:");
            System.out.println(stockData);
        } catch (Exception e) {
            System.err.println("Error fetching stock data: " + e.getMessage());
            throw e;
        }
    }

    private static class StockData {
        public String date;
        public double open;
        public double high;
        public double low;
        public double close;
        public long volume;

        @Override
        public String toString() {
            return String.format("Date: %s\nOpen: $%.2f\nHigh: $%.2f\nLow: $%.2f\nClose: $%.2f\nVolume: %d\n",
                date, open, high, low, close, volume);
        }
    }
}