package com.example.handmademarket.controller;

import com.example.handmademarket.dto.CreateEvaluationRequest;
import com.example.handmademarket.service.EvaluationService;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<ResponseResult> submitEvaluation(
            @RequestBody CreateEvaluationRequest request,
            @RequestHeader("X-User-Id") Long evaluatorId) {
        return ResponseEntity.ok(evaluationService.submitEvaluation(request, evaluatorId));
    }

    @GetMapping("/goods/{goodsId}")
    public ResponseEntity<ResponseResult> getEvaluationsByGoods(
            @PathVariable Long goodsId,
            @RequestParam(required = false) Integer sortBy) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByGoods(goodsId, sortBy));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseResult> getEvaluationsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer sortBy) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByUser(userId, sortBy));
    }

    @PostMapping("/{evalId}/violation")
    public ResponseEntity<ResponseResult> handleViolationReport(
            @PathVariable String evalId,
            @RequestParam Integer action,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(evaluationService.handleViolationReport(evalId, action, reason));
    }

    @GetMapping("/violations/reports")
    public ResponseEntity<ResponseResult> getViolationReports() {
        return ResponseEntity.ok(evaluationService.getViolationReports());
    }
}
