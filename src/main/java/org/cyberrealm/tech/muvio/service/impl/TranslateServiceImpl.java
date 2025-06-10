package org.cyberrealm.tech.muvio.service.impl;

import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.service.TranslateService;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TranslateServiceImpl implements TranslateService {
    private static final String LIBRE_TRANSLATE_URL = "https://libretranslate.com/translate";
    private final HttpClient httpClient;

    @Override
    public String translate(String text, String lang) {
        try {
            Thread.sleep(2500);
            String form = String.format("q=%s&source=en&target=%s&format=text",
                    URLEncoder.encode(text, StandardCharsets.UTF_8),
                    URLEncoder.encode(lang, StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LIBRE_TRANSLATE_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            System.out.println("json " + json);
            return json.getString("translatedText");

        } catch (Exception e) {
            System.err.println("LibreTranslate error: " + e.getMessage());
        }
        return null;
    }
}
