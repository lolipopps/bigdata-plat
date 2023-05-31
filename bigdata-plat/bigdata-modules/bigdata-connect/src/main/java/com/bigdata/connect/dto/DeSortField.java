package com.bigdata.connect.dto;

import com.bigdata.connect.entity.DatasetTableField;
import lombok.Data;

@Data
public class DeSortField extends DatasetTableField {

    private String orderDirection;
}
