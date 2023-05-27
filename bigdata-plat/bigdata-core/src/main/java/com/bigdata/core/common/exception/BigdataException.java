package com.bigdata.core.common.exception;

import lombok.Data;

/**
 * @author Bigdata
 */
@Data
public class BigdataException extends RuntimeException {

    private String msg;

    public BigdataException(String msg) {
        super(msg);
        this.msg = msg;
    }
}
