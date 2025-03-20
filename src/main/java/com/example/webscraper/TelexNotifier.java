package com.example.webscraper;

import com.example.webscraper.Bot.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.util.*;
import java.util.List;

@Component
public class TelexNotifier {

    private static final String TELEX_URL = "https://ping.telex.im/v1/webhooks/019582d6-476b-7d12-8721-37f9ebf858b4";
    private static final Logger logger = LoggerFactory.getLogger(TelexNotifier.class);
    private final RestTemplate restTemplate;

    public TelexNotifier(){
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(50000); // 20 seconds
        factory.setReadTimeout(100000); // 20 seconds
        return new RestTemplate(factory);
    }

    public void sendMessage(String channelId, String message) {
        sendToTelex(channelId, message);
    }

    public void sendInteractiveMessage(String channelId, String message, List<Button> buttons) {
        if (channelId == null || channelId.isEmpty()) {
            logger.error("Cannot send message: channel_id is missing.");
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event_name", "web_scraper");
        payload.put("username", "noxxspring");
        payload.put("status", "success");
        payload.put("message", message);
        payload.put("channel_id", channelId);

        // Properly format buttons as a JSON list
        List<Map<String, String>> buttonList = new ArrayList<>();
        for (Button button : buttons) {
            Map<String, String> btn = new HashMap<>();
            btn.put("text", button.getText());
            btn.put("value", button.getValue());
            buttonList.add(btn);
        }
        payload.put("buttons", buttonList); // Change key to "buttons" to match expected format

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        logger.info("Sending interactive message to Telex: {}", payload);

        ResponseEntity<String> response = restTemplate.postForEntity(TELEX_URL, entity, String.class);
        logger.info("Telex Response: Status code = {}, Body = {}", response.getStatusCode(), response.getBody());
    }

    public void sendToTelex(String channelId, String message) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("event_name", "web_scraper");
            payload.put("username", "noxxspring");
            payload.put("status", "success");
            payload.put("message", message);
            payload.put("channel_id", channelId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            logger.info("Sending message to Telex: {}", payload);

            ResponseEntity<String> response = restTemplate.postForEntity(TELEX_URL, entity, String.class);
            logger.info("Telex Response: Status code = {}, Body = {}", response.getStatusCode(), response.getBody());
        } catch (Exception e) {
            logger.error("Failed to send Telex notification: {}", e.getMessage(), e);
        }
    }
}
