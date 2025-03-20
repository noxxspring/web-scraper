package com.example.webscraper.BrokenAndDuplicateLinks;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class BrokenLinkAndDuplicateTracker {

    private final Map<String, List<String>> brokenLinksScan = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> duplicateLinksScan = new ConcurrentHashMap<>();

    public void logBrokenLink(String scanId, String url){
        brokenLinksScan.computeIfAbsent(scanId, k -> new CopyOnWriteArrayList<>()).add(url);
        System.out.println("📌 Logged Broken Link: " + url);
    }

    public void logDuplicateLink (String scanId, String url){
        duplicateLinksScan.computeIfAbsent(scanId, k -> new ConcurrentHashMap<>())
                .merge(url, 1, Integer::sum);
        System.out.println("🔄 Logged Duplicate Link: " + url);
    }

    public List<String> getBrokenList (String scanId){
        return brokenLinksScan.getOrDefault(scanId, List.of());
    }

    public Map<String, Integer> getDuplicateLinks(String scanId){
        return duplicateLinksScan.getOrDefault(scanId, Map.of());
    }

    public void clearLinks(String scanId){
        brokenLinksScan.remove(scanId);
        duplicateLinksScan.remove(scanId);
    }

    public String generateReport(String url, String scanId){
        List<String> brokenLinks = getBrokenList(scanId);
        Map<String, Integer> duplicateLink = getDuplicateLinks(scanId);

        StringBuilder report = new StringBuilder("❌ **Broken & Duplicate Links Report for:** ").append(url).append("\n\n");

        // Broken Links Section
        report.append("🚨 **Broken Links:**\n");
        if (brokenLinks.isEmpty()) {
            report.append("✅ No broken links found!\n\n");
        } else {
            brokenLinks.forEach(link -> report.append("- ").append(link).append("\n"));
        }

        // Duplicate Links Section
        report.append("\n🔄 **Duplicate Links:**\n");
        if (duplicateLink.isEmpty()) {
            report.append("✅ No duplicate links found!\n\n");
        } else {
            duplicateLink.forEach((link, count) -> report.append("- ").append(link).append(" (Appeared ").append(count).append(" times)\n"));
        }

        // Summary
        report.append("\n📊 **Summary:**\n");
        report.append("Total Broken Links: ").append(brokenLinks.size()).append("\n");
        report.append("Total Duplicate Links: ").append(duplicateLink.size()).append("\n");

        return report.toString();
    }
}
