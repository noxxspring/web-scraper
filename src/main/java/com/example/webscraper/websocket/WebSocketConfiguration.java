package com.example.webscraper.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private TelexWebSocketClient telexWebSocketClient;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

    }

    public void startWebSocketConnection(){
        telexWebSocketClient.connect();
    }
}
