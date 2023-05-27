package com.bigdata.core.common.exception;

import lombok.Data;

/**
 * @author Bigdata
 */
@Data
public class CaptchaException extends RuntimeException {

    private String msg;

    public CaptchaException(String msg) {
        super(msg);
        this.msg = msg;
    }
}
