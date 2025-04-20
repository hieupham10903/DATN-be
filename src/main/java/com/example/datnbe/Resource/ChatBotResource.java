package com.example.datnbe.Resource;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatBotResource {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";

    // Lưu lịch sử hội thoại trong bộ nhớ tạm (có thể thay bằng cơ sở dữ liệu)
    private final List<Map<String, String>> chatHistory = new ArrayList<>();

    @PostMapping
    public Map<String, String> getChatbotResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Thêm tin nhắn của user vào lịch sử
        chatHistory.add(Map.of("role", "user", "content", userMessage));

        // Chuyển danh sách tin nhắn thành một chuỗi duy nhất
        String historyAsString = formatChatHistory(chatHistory);

        // Gửi tin nhắn với lịch sử hội thoại
        String botReply = callMistral(historyAsString);

        // Lưu câu trả lời của chatbot
        chatHistory.add(Map.of("role", "assistant", "content", botReply));

        Map<String, String> response = new HashMap<>();
        response.put("reply", botReply);
        return response;
    }

    private String callMistral(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "mistral");
        requestBody.put("prompt", prompt); // Gửi cả lịch sử dưới dạng chuỗi
        requestBody.put("stream", false);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(OLLAMA_API_URL, HttpMethod.POST, entity, Map.class);

        // Debug response từ Ollama
        System.out.println("Ollama Response: " + response.getBody());

        if (response.getBody() != null && response.getBody().containsKey("response")) {
            return response.getBody().get("response").toString();
        }
        return "Xin lỗi, tôi không thể trả lời ngay bây giờ.";
    }

    private String formatChatHistory(List<Map<String, String>> history) {
        StringBuilder formattedHistory = new StringBuilder();
        for (Map<String, String> message : history) {
            formattedHistory.append(message.get("role")).append(": ").append(message.get("content")).append("\n");
        }
        return formattedHistory.toString();
    }
}
