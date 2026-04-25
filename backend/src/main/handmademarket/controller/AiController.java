package com.example.handmademarket.controller;

import com.example.handmademarket.service.BaiduImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final BaiduImageService baiduImageService;

    // 图片识别 AI 接口
    @PostMapping("/image-recognition")
    public ResponseEntity<?> imageRecognition(@RequestParam("file") MultipartFile file) {
        try {
            List<Map<String, Object>> result = baiduImageService.recognizeImage(file.getBytes());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("识别失败：" + e.getMessage());
        }
    }
}