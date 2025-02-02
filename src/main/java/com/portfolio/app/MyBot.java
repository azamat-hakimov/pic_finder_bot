package com.portfolio.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

public class MyBot extends TelegramLongPollingBot {

    private static final String ACCESS_KEY = "ff70c5db53msh5c5bcf64b96f569p1f8e5fjsn93ec0707e62b";
    private static final String BASE_URL = "unsplash-image-search-api.p.rapidapi.com";

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String text = message.getText();
            SendMessage response = new SendMessage();
            response.setChatId(message.getChatId().toString());

            if (text.equals("/start")) {
                response.setText("Hello, I'm your Picture Finder bot! Send me a keyword to find images.");
            } else {
                try {
                    String imageUrl = fetchImageUrl(text); // fetch image URL from Unsplash API
                    if (imageUrl != null) {
                        sendImage(message.getChatId(), imageUrl); // send the image back to the user
                    } else {
                        response.setText("❌ No image found for: " + text);
                        execute(response);
                    }
                } catch (IOException e) {
                    response.setText("⚠️ Error fetching image.");
                    try {
                        execute(response);
                    } catch (TelegramApiException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                execute(response);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private String fetchImageUrl(String query) throws IOException {
        String url = "https://unsplash-image-search-api.p.rapidapi.com/search?page=1&query=" + query;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", ACCESS_KEY)
                .addHeader("x-rapidapi-host", BASE_URL)
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            String jsonResponse = response.body().string();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            if (jsonNode.has("data") && jsonNode.get("data").has("results") && jsonNode.get("data").get("results").size() > 0) {
                return jsonNode.get("data").get("results").get(0).get("urls").get("regular").asText();
            }
        }
        return null;
    }

    private void sendImage(Long chatId, String imageUrl) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId.toString());
        photo.setPhoto(new org.telegram.telegrambots.meta.api.objects.InputFile(imageUrl));

        try {
            execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "@free_picture_finder_bot";
    }

    @Override
    public String getBotToken() {
        return "8108617331:AAG3FhNadwTj8dvg8b82uUXPnW60kWJmXfk";
    }
}
