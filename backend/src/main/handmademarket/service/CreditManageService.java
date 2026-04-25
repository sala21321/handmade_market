package com.example.handmademarket.service;

import com.example.handmademarket.entity.CreditRecord;
import com.example.handmademarket.entity.UserCredit;
import com.example.handmademarket.repository.CreditRecordRepository;
import com.example.handmademarket.repository.UserCreditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CreditManageService {

    @Autowired
    private UserCreditRepository userCreditRepository;

    @Autowired
    private CreditRecordRepository creditRecordRepository;

    // 查询全部用户信用列表（后台管理员查看）
    public List<UserCredit> getAllUserCredit() {
        return userCreditRepository.findAll();
    }

    // 根据用户ID查询单个信用信息
    public UserCredit getUserCreditByUserId(Long userId) {
        return userCreditRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户信用数据不存在"));
    }

    // 管理员信用加减分+自动日志记录
    @Transactional(rollbackFor = Exception.class)
    public void adjustUserCredit(Long userId, Long adminId, Integer scoreChange, String reason) {
        // 1. 获取用户现有信用数据
        UserCredit credit = userCreditRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("该用户信用数据不存在"));

        // 2. 计算并更新最新分数
        int finalScore = credit.getCreditScore() + scoreChange;
        if (finalScore > 100) {
            finalScore = 100;
        }
        credit.setCreditScore(finalScore);

        // 3. 自动判定更新信用等级与状态
        if (finalScore >= 90) {
            credit.setCreditLevel("高信用");
        } else if (finalScore >= 60) {
            credit.setCreditLevel("普通信用");
        } else {
            credit.setCreditLevel("低信用");
            credit.setStatus(0);
        }

        credit.setUpdateTime(new Date());
        userCreditRepository.save(credit);

        // 4. 永久留存管理员操作记录，可追溯
        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setAdminId(adminId);
        record.setScoreChange(scoreChange);
        record.setReason(reason);
        record.setCreateTime(new Date());
        creditRecordRepository.save(record);
    }

    // 信用申诉 - 普通管理员初审
    public void appealFirstCheck(Long recordId, Integer status) {
        // status: 1-通过初审 2-驳回
        Optional<CreditRecord> recordOptional = creditRecordRepository.findById(recordId);
        if (recordOptional.isEmpty()) {
            throw new RuntimeException("申诉记录不存在");
        }
        CreditRecord record = recordOptional.get();

        String result = status == 1 ? "【初审：通过】" : "【初审：驳回】";
        record.setReason(record.getReason() + result);
        creditRecordRepository.save(record);
    }

    // 信用申诉 - 超级管理员终审
    @Transactional
    public void appealFinalCheck(Long userId, Long recordId, Integer finalStatus, Integer fixScore) {
        // 仅超级管理员可调用
        if (finalStatus != 1) {
            throw new RuntimeException("申诉终审驳回");
        }

        // 终审通过 → 修正信用分
        UserCredit credit = getUserCreditByUserId(userId);
        int newScore = credit.getCreditScore() + fixScore;
        if (newScore > 100) {
            newScore = 100;
        }
        credit.setCreditScore(newScore);

        // 重新判定信用等级
        if (newScore >= 90) {
            credit.setCreditLevel("高信用");
        } else if (newScore >= 60) {
            credit.setCreditLevel("普通信用");
        } else {
            credit.setCreditLevel("低信用");
            credit.setStatus(0);
        }

        credit.setUpdateTime(new Date());
        userCreditRepository.save(credit);

        // 更新日志
        CreditRecord record = creditRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        record.setReason(record.getReason() + "【终审通过，修正分数：" + fixScore + "】");
        creditRecordRepository.save(record);
    }
}
