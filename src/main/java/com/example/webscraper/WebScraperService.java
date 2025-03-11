package com.example.webscraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebScraperService {
    private static final int TIMEOUT = 10000; // 10 seconds

    public Document scrape(String url) throws IOException {
        return Jsoup.connect(url).timeout(TIMEOUT).get();
    }

    public List<String> findBrokenLinksWithLines(Document document) {
        List<String> brokenLinks = new ArrayList<>();
        Elements links = document.select("a[href]");

        if (links.isEmpty()) {
            brokenLinks.add("No links found on the page.");
        }

        links.forEach(link -> {
            String url = link.absUrl("href");
            int lineNumber = link.siblingIndex() + 1;
            try {
                Connection.Response response = Jsoup.connect(url).timeout(TIMEOUT).ignoreHttpErrors(true).execute();
                if (response.statusCode() >= 400) {
                    brokenLinks.add("Broken Link: " + url + " (Line: " + lineNumber + ", Status: " + response.statusCode() + ")");
                }
            } catch (IOException e) {
                brokenLinks.add("Broken Link: " + url + " (Line: " + lineNumber + ", Error: " + e.getMessage() + ")");
            }
        });

        if (brokenLinks.isEmpty()) {
            brokenLinks.add("No broken links found.");
        }

        return brokenLinks;
    }

    public List<String> checkMissingMetaTagsWithLines(Document document) {
        List<String> missingTags = new ArrayList<>();
        Elements metaTags = document.select("meta[name]");

        if (metaTags.isEmpty()) {
            missingTags.add("No meta tags found.");
        }

        if (document.selectFirst("meta[name=description]") == null) {
            missingTags.add("Missing Meta Tag: description (Line: Unknown)");
        }

        if (document.selectFirst("meta[name=keywords]") == null) {
            missingTags.add("Missing Meta Tag: keywords (Line: Unknown)");
        }

        if (missingTags.isEmpty()) {
            missingTags.add("No missing meta tags found.");
        }

        return missingTags;
    }

    public List<String> findImagesWithIssuesAndLines(Document document) {
        List<String> imageIssues = new ArrayList<>();
        Elements images = document.select("img");

        if (images.isEmpty()) {
            imageIssues.add("No images found on the page.");
            return imageIssues;
        }

        images.forEach(img -> {
            String src = img.absUrl("src");
            String alt = img.attr("alt");
            int lineNumber = img.siblingIndex() + 1;

            if (src.isBlank()) {
                imageIssues.add("Image Issue: Empty src attribute (Line: " + lineNumber + ")");
            } else {
                try {
                    Connection.Response response = Jsoup.connect(src).timeout(TIMEOUT).ignoreHttpErrors(true).execute();
                    if (response.statusCode() >= 400) {
                        imageIssues.add("Broken Image: View Image (" + src + ") — Line: " + lineNumber + ", Status: " + response.statusCode());
                    }
                } catch (IOException e) {
                    imageIssues.add("Broken Image: View Image (" + src + ") — Line: " + lineNumber + ", Error: " + e.getMessage());
                }
            }

            if (alt == null || alt.isBlank()) {
                imageIssues.add("Missing Alt Attribute: View Image (" + src + ") — Line: " + lineNumber);
            }
        });

        if (imageIssues.isEmpty()) {
            imageIssues.add("All images are valid with alt attributes.");
        }

        return imageIssues;
    }

    public List<String> findDuplicateMetaTagsWithLines(Document document) {
        Map<String, Integer> metaTagCounts = new HashMap<>();
        List<String> duplicateMetaTags = new ArrayList<>();
        Elements metaTags = document.select("meta[name]");

        if (metaTags.isEmpty()) {
            duplicateMetaTags.add("No meta tags found.");
        }

        metaTags.forEach(tag -> {
            String metaName = tag.attr("name");
            int lineNumber = tag.siblingIndex() + 1;
            metaTagCounts.put(metaName, metaTagCounts.getOrDefault(metaName, 0) + 1);

            if (metaTagCounts.get(metaName) > 1) {
                duplicateMetaTags.add("Duplicate Meta Tag: " + metaName + " (Line: " + lineNumber + ")");
            }
        });

        if (duplicateMetaTags.isEmpty()) {
            duplicateMetaTags.add("No duplicate meta tags found.");
        }

        return duplicateMetaTags;
    }
    public String checkTitleTagWithLine(Document document) {
        Element titleTag = document.selectFirst("title");
        if (titleTag == null) {
            return "Title tag is missing (Line: Unknown)";
        }
        int lineNumber = titleTag.siblingIndex() + 1;
        return "Title tag is present (Line: " + lineNumber + ")";
    }

}

