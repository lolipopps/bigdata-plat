package com.bigdata.core.common.exception;

import lombok.Data;

/**
 * @author Bigdata
 */
@Data
public class LimitException extends RuntimeException {

    private String msg;

    public LimitException(String msg) {
        super(msg);
        this.msg = msg;
    }
}
