package com.example.handmademarket.dto;

public class AdminRequest {
    private Integer adminId;
    private String adminAccount;
    private String adminPwd;
    private String adminName;
    private Integer permissionLevel;

    // getter setter
    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }
    public String getAdminAccount() { return adminAccount; }
    public void setAdminAccount(String adminAccount) { this.adminAccount = adminAccount; }
    public String getAdminPwd() { return adminPwd; }
    public void setAdminPwd(String adminPwd) { this.adminPwd = adminPwd; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public Integer getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(Integer permissionLevel) { this.permissionLevel = permissionLevel; }
}
