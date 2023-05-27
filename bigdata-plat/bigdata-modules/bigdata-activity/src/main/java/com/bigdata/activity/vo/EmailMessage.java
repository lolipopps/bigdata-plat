package com.bigdata.activity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Bigdata
 */
@Data
@Accessors(chain = true)
public class EmailMessage implements Serializable {

    private String content;

    private String fullUrl;
}
