package com.example.datnbe.Resource;

import com.example.datnbe.Entity.Categories;
import com.example.datnbe.Entity.ChatMessage;
import com.example.datnbe.Entity.Criteria.ProductCriteria;
import com.example.datnbe.Entity.DTO.ProductsDTO;
import com.example.datnbe.Repository.CategoriesRepository;
import com.example.datnbe.Repository.ChatMessageRepository;
import com.example.datnbe.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import tech.jhipster.service.filter.BigDecimalFilter;
import tech.jhipster.service.filter.StringFilter;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/chatbot")
public class ChatBotResource {

    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @PostMapping
    public Map<String, String> getChatbotResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        List<ChatMessage> historyFromDb = chatMessageRepository.findAllByOrderByIdAsc();
        List<Map<String, String>> chatHistory = new ArrayList<>();

        for (ChatMessage msg : historyFromDb) {
            chatHistory.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        chatHistory.add(Map.of("role", "user", "content", userMessage));

        Map<String, String> keywords = extractSearchKeywords(userMessage);
        if (!keywords.isEmpty()) {
            List<ProductsDTO> matchedProducts = searchProducts(keywords);

            if (!matchedProducts.isEmpty()) {
                String productInfo = buildProductInfoString(matchedProducts);
                chatHistory.add(Map.of("role", "system", "content", "Đây là các sản phẩm phù hợp:\n" + productInfo));
            } else {
                chatHistory.add(Map.of("role", "system", "content", "Không tìm thấy sản phẩm phù hợp với yêu cầu."));
            }
        }

        String historyAsString = formatChatHistory(chatHistory);
        String botReply = callMistral(historyAsString);

        Map<String, String> response = new HashMap<>();
        response.put("reply", botReply);
        return response;
    }

    private String formatCurrencyVND(BigDecimal price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    private String removeVietnameseAccent(String str) {
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }

    private Map<String, String> extractSearchKeywords(String input) {
        Map<String, String> keywords = new HashMap<>();
        String lowercase = input.toLowerCase();

        // Xử lý giá: "dưới 5 triệu"
        if (lowercase.contains("dưới")) {
            String[] parts = lowercase.split("dưới");
            if (parts.length > 1) {
                String after = parts[1];
                boolean isTrieu = after.contains("triệu");
                String number = after.replaceAll("[^0-9]", "");
                if (!number.isEmpty()) {
                    keywords.put("maxPrice", number);
                    keywords.put("priceUnit", isTrieu ? "trieu" : "vnd");
                }
            }
        }

        // Xử lý tên sản phẩm
        if (lowercase.contains("tên là")) {
            String[] parts = lowercase.split("tên là");
            if (parts.length > 1) {
                keywords.put("name", parts[1].trim());
            }
        } else if (lowercase.matches(".*(sản phẩm|mua|tư vấn).*(\\w+).*")) {
            String[] tokens = lowercase.split(" ");
            if (tokens.length >= 1) {
                keywords.put("name", tokens[tokens.length - 1].trim());
            }
        }

        // Tìm theo tên danh mục (có dấu/không dấu)
        String cleanedInput = removeVietnameseAccent(lowercase);
        List<Categories> categories = categoriesRepository.findAll();
        for (Categories cat : categories) {
            String catName = cat.getName();
            String catNameClean = removeVietnameseAccent(catName.toLowerCase());

            if (cleanedInput.contains(catNameClean)
                    || cleanedInput.contains("danh muc " + catNameClean)
                    || cleanedInput.contains("loai san pham " + catNameClean)
                    || cleanedInput.contains("loai " + catNameClean)) {
                keywords.put("categoryId", cat.getId());
                break;
            }
        }

        return keywords;
    }

    private List<ProductsDTO> searchProducts(Map<String, String> keywords) {
        ProductCriteria criteria = new ProductCriteria();

        if (keywords.containsKey("name")) {
            String name = keywords.get("name").trim();
            if (!name.isEmpty()) {
                StringFilter nameFilter = new StringFilter();
                nameFilter.setContains(name);
                criteria.setName(nameFilter);
            }
        }

        if (keywords.containsKey("categoryId")) {
            StringFilter categoryFilter = new StringFilter();
            categoryFilter.setEquals(keywords.get("categoryId"));
            criteria.setCategoryId(categoryFilter);
        }

        if (keywords.containsKey("maxPrice")) {
            try {
                BigDecimal rawPrice = new BigDecimal(keywords.get("maxPrice").trim());

                if ("trieu".equalsIgnoreCase(keywords.get("priceUnit"))) {
                    rawPrice = rawPrice.multiply(BigDecimal.valueOf(1_000_000));
                }

                BigDecimalFilter priceFilter = new BigDecimalFilter();
                priceFilter.setLessThanOrEqual(rawPrice);
                criteria.setPrice(priceFilter);
            } catch (NumberFormatException | NullPointerException e) {
                System.err.println("Giá trị maxPrice không hợp lệ: " + keywords.get("maxPrice"));
            }
        }


        return productService.findByCriteria(criteria, Pageable.unpaged()).getContent();
    }

    private String buildProductInfoString(List<ProductsDTO> products) {
        StringBuilder sb = new StringBuilder();
        for (ProductsDTO p : products) {
            String productLink = "https://datn-fe-client.vercel.app/product/" + p.getId();
            sb.append("- ")
                    .append(p.getName()).append(" (")
                    .append(formatCurrencyVND(p.getPrice())).append(", ")
                    .append("danh mục: ").append(p.getCategoryName()).append(", ")
                    .append("kho: ").append(p.getWarehouseName()).append(")\n")
                    .append("  👉 Xem chi tiết: ").append(productLink).append("\n\n");
        }
        return sb.toString();
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

        if (response.getBody() != null && response.getBody().containsKey("response")) {
            return response.getBody().get("response").toString();
        }
        return "Xin lỗi, tôi không thể trả lời ngay bây giờ.";
    }

    private String formatChatHistory(List<Map<String, String>> history) {
        StringBuilder formattedHistory = new StringBuilder();
        for (Map<String, String> message : history) {
            formattedHistory.append(message.get("role"))
                    .append(": ")
                    .append(message.get("content"))
                    .append("\n");
        }
        return formattedHistory.toString();
    }
}
