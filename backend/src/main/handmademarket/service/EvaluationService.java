package com.example.handmademarket.service;

import com.example.handmademarket.dto.CreateEvaluationRequest;
import com.example.handmademarket.util.ResponseResult;

public interface EvaluationService {

    ResponseResult submitEvaluation(CreateEvaluationRequest request, Long evaluatorId);

    ResponseResult getEvaluationsByGoods(Long goodsId, Integer sortBy);

    ResponseResult getEvaluationsByUser(Long userId, Integer sortBy);

    ResponseResult handleViolationReport(String evalId, Integer action, String reason);

    ResponseResult getViolationReports();
}
