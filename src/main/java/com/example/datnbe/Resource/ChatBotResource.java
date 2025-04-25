package com.example.datnbe.Resource;

import com.example.datnbe.Entity.ChatMessage;
import com.example.datnbe.Repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatBotResource {

    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @PostMapping
    public Map<String, String> getChatbotResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Lấy lịch sử từ CSDL
        List<ChatMessage> historyFromDb = chatMessageRepository.findAllByOrderByIdAsc();

        // Tạo danh sách lịch sử tạm thời (chỉ trong bộ nhớ, không lưu vào db)
        List<Map<String, String>> chatHistory = new ArrayList<>();
        for (ChatMessage msg : historyFromDb) {
            chatHistory.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        // Thêm câu hỏi của người dùng vào lịch sử tạm thời
        chatHistory.add(Map.of("role", "user", "content", userMessage));

        // Định dạng lịch sử trò chuyện thành chuỗi để gửi đến Mistral
        String historyAsString = formatChatHistory(chatHistory);

        // Gọi API của Mistral để nhận phản hồi từ chatbot
        String botReply = callMistral(historyAsString);

        // Trả về phản hồi từ chatbot mà không lưu vào cơ sở dữ liệu
        Map<String, String> response = new HashMap<>();
        response.put("reply", botReply);
        return response;
    }

    private String callMistral(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama3.2");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(OLLAMA_API_URL, HttpMethod.POST, entity, Map.class);

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
