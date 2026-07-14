package kg.restaurant.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.chat.id:}")
    private String chatId;

    public void sendMessage(String text) {
        if (botToken == null || botToken.isBlank()
                || chatId == null || chatId.isBlank()) {
            log.warn("Telegram иштебейт: bot token же chat id бош");
            return;
        }

        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(
                    Map.of(
                            "chat_id", chatId,
                            "text", text
                    ),
                    headers
            );

            String response = restTemplate.postForObject(url, request, String.class);

            if (response != null && response.contains("\"ok\":false")) {
                log.error("Telegram API катасы: {}", response);
            } else {
                log.info("Telegram билдирүү жиберildi");
            }

        } catch (Exception e) {
            log.error("Telegram катасы: {}", e.getMessage());
        }
    }
}
