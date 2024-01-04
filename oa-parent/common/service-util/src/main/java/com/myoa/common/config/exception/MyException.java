package com.myoa.common.config.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
public class MyException extends RuntimeException {
    private Integer code;
    private String msg;
    public MyException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
