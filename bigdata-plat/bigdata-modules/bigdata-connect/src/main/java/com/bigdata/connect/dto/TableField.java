package com.bigdata.connect.dto;

import lombok.Data;

@Data
public class TableField {
    private String fieldName;
    private String remarks;
    private String fieldType;
    private int fieldSize;
    private int accuracy;

}
