package com.example.webscraper;

import com.example.webscraper.Bot.Bot;
import com.example.webscraper.Bot.TelexEvent;
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
import java.util.*;

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
    private final Bot bot;

    public WebScraperController(TelexNotifier telexNotifier, WebScraperService webScraperService, Bot bot) {
        this.telexNotifier = telexNotifier;
        this.webScraperService = webScraperService;
        this.bot = bot;
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
        String url = payload.getUrl();
        String scanId = UUID.randomUUID().toString(); // generate a unique scan Id
        try {

            Document document = webScraperService.scrape(url);
            if (document == null) {
                logger.error("Failed to scrape content from: {}", url);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to scrape content from: " + url);
            }
            String seoReport = webScraperService.generateSeoReport(url,scanId, payload.getChannelId());
            // Send report to Telex
            telexNotifier.sendToTelex(payload.getChannelId(), seoReport);
            logger.info("Sent detailed SEO report to Telex: \n{}", seoReport);

            return ResponseEntity.ok("Scraped SEO report sent to Telex.");
        } catch (IOException e) {
            logger.error("Failed to send SEO report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send scraped report: " + e.getMessage());
        }
    }

    @PostMapping("/webhook/telex")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String,String> payload) {

        String text = payload.get("text");
        String channelId = payload.get("channel_id"); // Extract channel ID

        if (text != null) {
            TelexEvent telex = new TelexEvent();
            telex.setText(text);
            telex.setChannelId(channelId != null ? channelId : "default-channel-id"); // Ensure channelId is never null

            bot.handleEvent(telex);
        }

        return ResponseEntity.ok().build();
    }
}
















