package com.bigdata.autochat.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PromotionVo<T> {

    String code = "promotion";

    T data;
}
