package com.example.handmademarket.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "tb_credit_record") // 绑定数据库表名（必须加）
public class CreditRecord {

    // 主键 自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用户ID 匹配 user_id
    private Long userId;
    // 管理员ID 匹配 admin_id
    private Long adminId;
    // 分数变动 匹配 score_change
    private Integer scoreChange;
    // 原因 匹配 reason
    private String reason;
    // 创建时间 匹配 create_time
    private Date createTime;
}