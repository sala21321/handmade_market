package com.example.handmademarket.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "tb_user_credit") // 绑定数据库表名（必须加）
public class UserCredit {

    // 主键 自增（匹配数据库 AUTO_INCREMENT）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用户ID 匹配 user_id
    private Long userId;
    // 信用分数 匹配 credit_score
    private Integer creditScore;
    // 信用等级 匹配 credit_level
    private String creditLevel;
    // 状态 匹配 status
    private Integer status;
    // 更新时间 匹配 update_time
    private Date updateTime;
}