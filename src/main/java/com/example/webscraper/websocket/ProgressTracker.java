package com.example.webscraper.websocket;

import com.example.webscraper.TelexNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ProgressTracker {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TelexNotifier telexNotifier;

    public void sendProgress(String scanId,String channelId, int progress, String message){

    try {

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scanId", scanId);
        payload.put("progress", progress);
        payload.put("message", message);

        System.out.println("Sending WebSocket update: " + payload);


        messagingTemplate.convertAndSend("/topic/progress/" + scanId, payload);

        String telexMessage = "🔍 **Scanning Progress**\n"
                + "------------------------------\n"
                + "📊 **Progress:** `" + progress + "%`\n"
                + "📢 **Status:** `" + message + "`\n"
                + "------------------------------";

        // Log Telex updates for debugging
        System.out.println("📩 Sending Telex update: " + telexMessage);

        telexNotifier.sendMessage(channelId, telexMessage);
        Thread.sleep(5000);
    }catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    }

    public void sendReport(String scanId, String channelId, String title, String reportContent) {
        if (channelId == null || channelId.isEmpty()) {
            System.err.println("❌ Cannot send Telex report: channel_id is missing.");
            return;
        }

        String formattedReport = "**" + title + "**\n\n" + reportContent;

        System.out.println("📩 Sending Telex Report: " + title);
        telexNotifier.sendMessage(channelId, formattedReport);
    }

    private String formatProgressMessage(int progress, String message) {
        return "🔍 **Scanning Progress**\n"
                + "------------------------------\n"
                + "📊 **Progress:** `" + progress + "%`\n"
                + "📢 **Status:** `" + message + "`\n"
                + "------------------------------";
    }

    public void sendAlert(String channelId, String alertMessage) {
        if (channelId == null || channelId.isEmpty()) {
            System.err.println("❌ Cannot send alert: channel_id is missing.");
            return;
        }

        System.out.println("🚨 Sending Telex Alert: " + alertMessage);
        telexNotifier.sendMessage(channelId, alertMessage);
    }

}
