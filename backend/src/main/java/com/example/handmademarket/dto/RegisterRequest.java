package com.example.handmademarket.dto;

public class RegisterRequest {

    private String useraccount;
    private String password;
    private String confirmPassword;
    private String email;
    private String role;
    private String phone;

    public String getUserAccount() {
        return useraccount;
    }

    public void setUserAccount(String useraccount) {
        this.useraccount = useraccount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone; 
    }

    public void setPhone(String phone) {
        this.phone = phone; 
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
