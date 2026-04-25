package com.example.handmademarket.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_admin")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;  // 管理员ID，自增

    @Column(name = "admin_account", unique = true, nullable = false)
    private String adminAccount;  // 管理员账号，唯一

    @Column(name = "admin_pwd", nullable = false)
    private String adminPwd;  // 管理员密码，加密存储

    @Column(name = "admin_name")
    private String adminName;  // 管理员真实姓名

    @Column(name = "permission_level")
    private Integer permissionLevel;  // 1-超级管理员 2-普通管理员

    @Column(name = "create_time")
    private Date createTime;  // 账号创建时间

    @Column(name = "update_time")
    private Date updateTime;  // 更新时间

    @Column(name = "status", columnDefinition = "INT DEFAULT 1")
    private Integer status;  // 0-禁用 1-正常

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminAccount() {
        return adminAccount;
    }

    public void setAdminAccount(String adminAccount) {
        this.adminAccount = adminAccount;
    }

    public String getAdminPwd() {
        return adminPwd;
    }

    public void setAdminPwd(String adminPwd) {
        this.adminPwd = adminPwd;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}