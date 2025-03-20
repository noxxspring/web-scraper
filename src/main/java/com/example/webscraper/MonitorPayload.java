package com.example.webscraper;

import java.util.List;

public class MonitorPayload {
    private String url;
    private String channelId = "019582d6-476b-7d12-8721-37f9ebf858b4";


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }


}
