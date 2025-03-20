package com.example.webscraper.Bot;

public class ScanResult {

    private String userId;
    private String url;
    private String seoReport;

    // Constructor, getters, and setters
    public ScanResult(String userId, String url, String seoReport) {
        this.userId = userId;
        this.url = url;
        this.seoReport = seoReport;
    }

    public String getUserId() {
        return userId;
    }

    public String getUrl() {
        return url;
    }

    public String getSeoReport() {
        return seoReport;
    }
}
