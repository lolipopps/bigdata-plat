package com.bigdata.activity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Bigdata
 */
@Data
public class ActPage<T> {

    List<T> content;

    Long totalElements;
}
