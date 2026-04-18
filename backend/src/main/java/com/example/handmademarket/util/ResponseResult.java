package com.example.handmademarket.util;

public class ResponseResult {

    private boolean success;
    private String message;
    private Object data;

    public ResponseResult() {
    }

    public ResponseResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ResponseResult ok(String message, Object data) {
        return new ResponseResult(true, message, data);
    }

    public static ResponseResult ok(String message) {
        return new ResponseResult(true, message, null);
    }

    public static ResponseResult ok(Object data, String token) {
        return new ResponseResult(true, "OK", data);
    }

    public static ResponseResult fail(String message) {
        return new ResponseResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
