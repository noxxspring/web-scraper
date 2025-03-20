package com.example.webscraper.Bot;

import com.example.webscraper.TelexNotifier;
import com.example.webscraper.WebScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class Bot {

    @Autowired
    private TelexNotifier telexNotifier;

    @Autowired
    private WebScraperService webScraperService;

    private final Map<String, ScanResult> scanResults = new HashMap<>(); // Store scan results


    public void handleEvent(TelexEvent telex) {
        String text = telex.getText();
        String channelId = telex.getChannelId();// Only use channelId now
        if (channelId == null || channelId.isEmpty()) {
            channelId = "default-channel-id"; // Ensure a valid channel ID
        }


        if (text.equalsIgnoreCase("start")) {
            sendWelcomeMessage(channelId);
        } else if (text.equalsIgnoreCase("yes")) {
            sendUrlPrompt(channelId);
        } else if (text.equalsIgnoreCase("no")) {
            sendGoodByeMessage(channelId);
        } else if (isValidUrl(text)) {
            String scanId = UUID.randomUUID().toString(); // Unique scan ID
            telexNotifier.sendMessage(channelId, "📡 **Your Scan ID:** `" + scanId + "`\nTracking progress now...");

            String userId = UUID.randomUUID().toString(); // Generate userId for future tracking

            String report = webScraperService.generateSeoReport(text, scanId,channelId);

            // Future: Save to DB (once added)
            // dbService.saveScan(userId, scanId, text);

            telexNotifier.sendToTelex(channelId, report);
        }
    }

    private void sendWelcomeMessage(String userId){
        String message = " Welcome! would you like to scan your URL?";
        List<Button> buttons = List.of(new Button("Yes","yes"), new Button("No","no"));
        telexNotifier.sendInteractiveMessage(userId,message, buttons);
    }

    private void sendUrlPrompt(String userId){
        String message = "Please enter the URL you want to scan:";
        telexNotifier.sendMessage(userId, message);
    }

    private void sendGoodByeMessage(String userId){
        String message = "Goodbye! Let me know if you want help later";
        telexNotifier.sendMessage(userId, message);
    }

    private boolean isValidUrl(String text){
        return text.startsWith("http://") || text.startsWith("https://");
    }
}
