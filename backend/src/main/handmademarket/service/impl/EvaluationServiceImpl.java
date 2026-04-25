package com.example.handmademarket.service.impl;

import com.example.handmademarket.dto.CreateEvaluationRequest;
import com.example.handmademarket.entity.Evaluation;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.repository.EvaluationRepository;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.service.EvaluationService;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private UserRepository userRepository;

    private boolean isViolationContent(String content) {
        String[] keywords = {"辱骂", "虚假", "广告", "无关"};
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResponseResult submitEvaluation(CreateEvaluationRequest request, Long evaluatorId) {
        if (request.getOrderId() == null) {
            return ResponseResult.fail("订单ID不能为空");
        }

        if (request.getEvaluatedId() == null) {
            return ResponseResult.fail("被评价人ID不能为空");
        }

        if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5) {
            return ResponseResult.fail("评分必须在1-5之间");
        }

        if (request.getContent() == null || request.getContent().isEmpty()) {
            return ResponseResult.fail("评价内容不能为空");
        }

        if (request.getContent().length() > 200) {
            return ResponseResult.fail("评价内容不能超过200字");
        }

        if (isViolationContent(request.getContent())) {
            return ResponseResult.fail("评价内容包含违规词汇，请修改后重新提交");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setEvalId(String.valueOf(System.currentTimeMillis()));
        evaluation.setOrderId(String.valueOf(request.getOrderId()));
        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setEvaluatedId(request.getEvaluatedId());
        evaluation.setScore(request.getScore());
        evaluation.setContent(request.getContent());
        evaluation.setImages(request.getImages());
        evaluation.setGoodsId(request.getGoodsId());
        evaluation.setStatus(0);
        evaluation.setCreateTime(LocalDateTime.now());

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);

        updateUserCredit(request.getEvaluatedId(), request.getScore());

        return ResponseResult.ok("评价提交成功", savedEvaluation);
    }

    @Override
    public ResponseResult getEvaluationsByGoods(Long goodsId, Integer sortBy) {
        List<Evaluation> evaluations = evaluationRepository.findByGoodsIdAndStatusValid(goodsId);

        if (sortBy != null) {
            switch (sortBy) {
                case 1:
                    evaluations.sort((a, b) -> b.getScore().compareTo(a.getScore()));
                    break;
                case 2:
                    evaluations.sort((a, b) -> a.getScore().compareTo(b.getScore()));
                    break;
                default:
                    break;
            }
        }

        List<Map<String, Object>> result = evaluations.stream().map(this::buildEvaluationMap).collect(Collectors.toList());
        return ResponseResult.ok(result);
    }

    @Override
    public ResponseResult getEvaluationsByUser(Long userId, Integer sortBy) {
        List<Evaluation> evaluations = evaluationRepository.findByUserIdAndStatusValid(userId);

        if (sortBy != null) {
            switch (sortBy) {
                case 1:
                    evaluations.sort((a, b) -> b.getScore().compareTo(a.getScore()));
                    break;
                case 2:
                    evaluations.sort((a, b) -> a.getScore().compareTo(b.getScore()));
                    break;
                default:
                    break;
            }
        }

        List<Map<String, Object>> result = evaluations.stream().map(this::buildEvaluationMap).collect(Collectors.toList());
        return ResponseResult.ok(result);
    }

    @Override
    public ResponseResult handleViolationReport(String evalId, Integer action, String reason) {
        Optional<Evaluation> optional = evaluationRepository.findByEvalId(evalId);
        if (!optional.isPresent()) {
            return ResponseResult.fail("评价不存在");
        }

        Evaluation evaluation = optional.get();

        if (action == 1) {
            evaluation.setStatus(2);
            evaluationRepository.save(evaluation);

            deductUserCredit(evaluation.getEvaluatorId(), 2);

            return ResponseResult.ok("违规评价已删除，评价人已扣2分信用分");
        } else if (action == 2) {
            evaluation.setStatus(0);
            evaluationRepository.save(evaluation);
            return ResponseResult.ok("举报已驳回");
        }

        return ResponseResult.fail("操作类型不正确");
    }

    @Override
    public ResponseResult getViolationReports() {
        List<Evaluation> reports = evaluationRepository.findViolationReports();
        List<Map<String, Object>> result = reports.stream().map(this::buildEvaluationMap).collect(Collectors.toList());
        return ResponseResult.ok(result);
    }

    private Map<String, Object> buildEvaluationMap(Evaluation eval) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("evalId", eval.getEvalId());
        map.put("evaluatorId", eval.getEvaluatorId());
        map.put("score", eval.getScore());
        map.put("content", eval.getContent());
        map.put("images", eval.getImages());
        map.put("createTime", eval.getCreateTime());
        map.put("scoreLevel", getScoreLevel(eval.getScore()));
        return map;
    }

    private String getScoreLevel(Integer score) {
        if (score >= 4) return "好评";
        if (score <= 2) return "差评";
        return "中评";
    }

    private void updateUserCredit(Long userId, Integer score) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int creditChange = 0;
            if (score >= 4) {
                creditChange = 1;
            } else if (score <= 2) {
                creditChange = -2;
            }

            if (creditChange != 0) {
                int currentCredit = user.getCreditScore() != null ? user.getCreditScore() : 0;
                user.setCreditScore(currentCredit + creditChange);
                userRepository.save(user);
            }
        }
    }

    private void deductUserCredit(Long userId, int amount) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int currentCredit = user.getCreditScore() != null ? user.getCreditScore() : 0;
            user.setCreditScore(Math.max(0, currentCredit - amount));
            userRepository.save(user);
        }
    }
}
