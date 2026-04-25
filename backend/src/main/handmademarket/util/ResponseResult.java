package com.example.handmademarket.util;

public class ResponseResult {

    private int code;
    private boolean success;
    private String message;
    private Object data;

    public ResponseResult() {
    }

    public ResponseResult(int code, boolean success, String message, Object data) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ResponseResult ok(String message, Object data) {
        return new ResponseResult(200, true, message, data);
    }

    public static ResponseResult ok(String message) {
        return new ResponseResult(200, true, message, null);
    }

    public static ResponseResult ok(Object data) {
        return new ResponseResult(200, true, "操作成功", data);
    }

    public static ResponseResult fail(String message) {
        return new ResponseResult(400, false, message, null);
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
