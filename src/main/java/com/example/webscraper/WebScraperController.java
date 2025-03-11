package com.example.webscraper;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WebScraperController {
    @Value("${telex.integration.app_name}")
    private String appName;

    @Value("${telex.integration.settings.interval}")
    private String interval;

    @Value("${telex.integration.app_description}")
    private String appDescription;

    @Value("${telex.integration.app_logo}")
    private String appLogo;

    @Value("${telex.integration.app_url}")
    private String appUrl;

    @Value("${telex.integration.background_color}")
    private String backgroundColor;

    @Value("${telex.integration.is_active}")
    private boolean isActive;

    @Value("${telex.integration.integration_type}")
    private String integrationType;

    @Value("${telex.integration.key_features}")
    private List<String> keyFeatures;

    @Value("${telex.integration.author}")
    private String author;

    @Value("${telex.integration.settings.time_interval}")
    private String timeInterval;

    @Value("${telex.integration.integration_category}")
    private String integrationCategory;

    @Value("${telex.integration.settings.event_type}")
    private String eventType;

    @Value("${telex.integration.target_url}")
    private String targetUrl;

    @Value("${telex.integration.tick_url}")
    private String tickUrl;

    private static final Logger logger = LoggerFactory.getLogger(WebScraperController.class);
    private final TelexNotifier telexNotifier;
    private final WebScraperService webScraperService;

    public WebScraperController(TelexNotifier telexNotifier, WebScraperService webScraperService) {
        this.telexNotifier = telexNotifier;
        this.webScraperService = webScraperService;
    }

    @GetMapping("/integration.json")
    public Map<String, Object> getIntegrationJson() {
        Map<String, Object> descriptions = new LinkedHashMap<>();
        descriptions.put("app_name", appName);
        descriptions.put("app_description", appDescription);
        descriptions.put("app_url", appUrl);
        descriptions.put("app_logo", appLogo);
        descriptions.put("background_color", backgroundColor);

        List<Map<String, Object>> settings = new ArrayList<>();

        Map<String, Object> timeIntervalSetting = new LinkedHashMap<>();
        timeIntervalSetting.put("label", "time_interval");
        timeIntervalSetting.put("type", "dropdown");
        timeIntervalSetting.put("required", true);
        timeIntervalSetting.put("default", "one-hour");

        Map<String, Object> eventTypeSetting = new LinkedHashMap<>();
        eventTypeSetting.put("label", "event_type");
        eventTypeSetting.put("type", "text");
        eventTypeSetting.put("required", true);
        eventTypeSetting.put("default", "* * * * *");

        Map<String, Object> intervalSetting = new LinkedHashMap<>();
        intervalSetting.put("label", "interval");
        intervalSetting.put("type", "text");
        intervalSetting.put("required", true);
        intervalSetting.put("default", "* * * * *");

        settings.add(timeIntervalSetting);
        settings.add(eventTypeSetting);
        settings.add(intervalSetting);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("descriptions", descriptions);
        data.put("integration_type", integrationType);
        data.put("is_active", isActive);
        data.put("integration_category", integrationCategory);
        data.put("key_features", keyFeatures);
        data.put("settings", settings);
        data.put("target_url", targetUrl);
        data.put("tick_url", tickUrl);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", data);

        logger.info("Returning integration JSON: {}", response);
        return response;
    }

    @PostMapping("/scrape")
    public ResponseEntity<String> scrapeWebsite(@RequestBody MonitorPayload payload) {
        try {
            Document document = webScraperService.scrape(payload.getUrl());
            if (document == null) {
                logger.error("Failed to scrape content from: {}", payload.getUrl());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to scrape content from: " + payload.getUrl());
            }

            List<String> brokenLinks = webScraperService.findBrokenLinksWithLines(document);
            List<String> missingMetaTags = webScraperService.checkMissingMetaTagsWithLines(document);
            List<String> imageIssues = webScraperService.findImagesWithIssuesAndLines(document);
            List<String> duplicateMetaTags = webScraperService.findDuplicateMetaTagsWithLines(document);
            boolean isTitleTagMissing = Boolean.parseBoolean(webScraperService.checkTitleTagWithLine(document));


            StringBuilder message = new StringBuilder("Scraping completed for " + payload.getUrl() + "\n\n");

            message.append("=== Issues Found ===\n");

            message.append("Broken Links:\n");
            brokenLinks.forEach(link -> message.append("- " + link + "\n"));
            if (brokenLinks.isEmpty()) {
                message.append("No broken links found.\n");
            }
            message.append("\n");

            message.append("Missing Meta Tags:\n");
            missingMetaTags.forEach(tag -> message.append("- " + tag + "\n"));
            if (missingMetaTags.isEmpty()) {
                message.append("No missing meta tags found.\n");
            }
            message.append("\n");

            message.append("Image Issues:\n");
            imageIssues.forEach(issue -> message.append("- " + issue + "\n"));
            if (imageIssues.isEmpty()) {
                message.append("No image issues found.\n");
            }
            message.append("\n");

            message.append("Duplicate Meta Tags:\n");
            duplicateMetaTags.forEach(tag -> message.append("- " + tag + "\n"));
            if (duplicateMetaTags.isEmpty()) {
                message.append("No duplicate meta tags found.\n");
            }
            message.append("\n");

            if (isTitleTagMissing) {
                message.append("Title tag is missing.\n");
            } else {
                message.append("Title tag is present.\n");
            }


            telexNotifier.sendToTelex(payload.getChannelId(), payload.getUrl(), brokenLinks, missingMetaTags, imageIssues, message.toString());
            logger.info("Success notification sent to Telex for Channel ID: {}", payload.getChannelId());
            return ResponseEntity.ok("scraped report sent to Telex.");
        } catch (IOException e) {
            logger.error("Failed to send audit report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send scraped report: " + e.getMessage());
        }
    }


}













