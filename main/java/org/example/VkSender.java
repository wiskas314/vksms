package org.example;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class VkSender {
    private static final String API_URL = "https://api.vk.ru/method/messages.send";
    private static final String API_VERSION = "5.199";

    private final HttpClient client = HttpClient.newHttpClient();

    public boolean send(String token, long peerId,String message){
        try{
            String body = "access_token="+token+
                    "&v"+API_VERSION+
                    "&peer_id="+peerId+
                    "&message="+ URLEncoder.encode(message, StandardCharsets.UTF_8)+
                    "&random_id"+ System.currentTimeMillis();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type","application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().contains("\"response\":");
        } catch (Exception e) {
            System.err.println("Ошибка отправки peer_id=" + peerId + ": " + e.getMessage());
            return false;
        }
    }
}
