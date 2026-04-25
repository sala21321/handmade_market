package com.example.handmademarket.dto;

public class LoginRequest {

    private String userAccount;   // 输入：手机号 或 userAccount / adminAccount
    private String password;
    private String code;      // 验证码（演示）
    private Integer type;    // 1=用户 2=管理员

    public LoginRequest() {
    }

    public LoginRequest(String userAccount, String password) {
        this.userAccount = userAccount;
        this.password = password;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
