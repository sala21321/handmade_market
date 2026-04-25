package com.example.handmademarket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BaiduImageService {

    @Value("${baidu.api-key:}")
    private String apiKey;

    @Value("${baidu.secret-key:}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;
    private long tokenExpireTime;

    /**
     * 获取百度API的access_token
     */
    private synchronized String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }
        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id="
                + apiKey + "&client_secret=" + secretKey;
        try {
            String response = restTemplate.postForObject(url, null, String.class);
            JsonNode node = objectMapper.readTree(response);
            accessToken = node.get("access_token").asText();
            int expiresIn = node.get("expires_in").asInt();
            tokenExpireTime = System.currentTimeMillis() + (expiresIn - 600) * 1000L;
            return accessToken;
        } catch (Exception e) {
            throw new RuntimeException("获取百度access_token失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用百度图像识别API
     */
    public List<Map<String, Object>> recognizeImage(byte[] imageBytes) {
        String token = getAccessToken();
        String url = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general?access_token=" + token;

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("image", base64Image);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            List<Map<String, Object>> results = new ArrayList<>();
            JsonNode resultNode = root.get("result");
            if (resultNode != null && resultNode.isArray()) {
                for (JsonNode item : resultNode) {
                    Map<String, Object> tag = new HashMap<>();
                    tag.put("keyword", item.get("keyword").asText());
                    tag.put("score", item.get("score").asDouble());
                    tag.put("root", item.has("root") ? item.get("root").asText() : "");
                    results.add(tag);
                }
            }
            // 按分数降序排列
            results.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
            return results;
        } catch (Exception e) {
            throw new RuntimeException("百度图像识别调用失败: " + e.getMessage(), e);
        }
    }
}
