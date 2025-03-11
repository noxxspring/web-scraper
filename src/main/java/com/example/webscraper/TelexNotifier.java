package com.example.webscraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TelexNotifier {

    private static final String TELEX_URL = "https://ping.telex.im/v1/webhooks/019582d6-476b-7d12-8721-37f9ebf858b4";
    private static final Logger logger = LoggerFactory.getLogger(TelexNotifier.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public void sendToTelex(String channelId, String url, List<String> brokenLinks, List<String> missingMetaTags, List<String> imageIssues, String message) {
        try {
            // Construct your message payload here
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("event_name", "web scraper");
            payload.put("username", "noxxspring");
            payload.put("status", "success");
            payload.put("message", message);
            payload.put("url", url);
            payload.put("broken_links", brokenLinks);
            payload.put("missing_meta_tags", missingMetaTags);
            payload.put("image_issues", imageIssues);
            payload.put("channel_id", channelId);



            // Send the constructed payload to Telex
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            logger.info("Sending message to Telex: {}", payload);

            ResponseEntity<String> response = restTemplate.postForEntity(TELEX_URL, entity, String.class);  // Modify targetUrl as per your setup

            logger.info("Telex Response: Status code = {}, Body = {}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                String responseBody = response.getBody();
                if (responseBody != null && responseBody.contains("\"status\":\"error\"")) {
                    logger.error("Telex API returned an error: {}", responseBody);
                } else {
                    logger.info("Telex notification sent successfully: {}", responseBody);
                }
            } else {
                logger.error("Failed to send Telex notification. Status code: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Failed to send Telex notification: {}", e.getMessage(), e);
        }
    }

}
