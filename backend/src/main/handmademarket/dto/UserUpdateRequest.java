package com.example.handmademarket.dto;

public class UserUpdateRequest {

    // 基础信息
    private String userAccount;
    private String userName;
    private String phone;
    private String email;
    private String address;
    private String avatar;
    private String specialty;

    private String oldPassword;
    private String newPassword;

    public String getUserAccount() { return userAccount; }

    public void setUserAccount(String userAccount) { this.userAccount = userAccount; }

    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    
    public void setAddress(String address) { this.address = address; }

    public String getAvatar() { return avatar; }

    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getSpecialty() { return specialty; }

    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getOldPassword() { return oldPassword; }

    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }

    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}