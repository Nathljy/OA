package com.myoa.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 状态码
    private String message; // 返回状态信息
    private T data; // 返回的相应数据

    // 对象私有化
    private Result() {}

    // 封装返回的数据
    public static<T> Result<T> build(T body, ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        if(body != null) {
            result.setData(body);
        }
        // 状态码
        result.setCode(resultCodeEnum.getCode());
        // 返回信息
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

    // "成功200"的方法
    public static<T> Result<T> ok(T data) {
        return Result.build(data, ResultCodeEnum.SUCCESS);
    }

    // "失败201"的方法
    public static<T> Result<T> fail(T data) {
        return Result.build(data, ResultCodeEnum.FAIL);
    }

    // 可以扩展
    public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
    }
    public Result<T> code(Integer code){
        this.setCode(code);
        return this;
    }

}
