
package com.example.handmademarket.util;

// 全局统一返回结果类 纯原生Java 无Lombok依赖
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    // 无参构造方法
    public Result() {
    }

    // 全参构造方法
    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 所有字段 Getter、Setter 方法（前端JSON序列化必须要）
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // 成功返回静态方法
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "查询成功", data);
    }

    // 失败返回静态方法
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
}


