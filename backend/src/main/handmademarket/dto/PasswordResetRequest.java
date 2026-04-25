package com.example.handmademarket.dto;

public class PasswordResetRequest {
    private String account; // 账号
    private String newPassword; // 必须加：新密码

    // 固定验证码
    public static final String FIXED_CODE = "a123456";

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    // 获取固定验证码
    public String getCode() {
        return FIXED_CODE;
    }
}