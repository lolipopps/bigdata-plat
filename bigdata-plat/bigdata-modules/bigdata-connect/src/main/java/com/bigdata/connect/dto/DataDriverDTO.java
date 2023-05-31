package com.bigdata.connect.dto;

import com.bigdata.connect.entity.DataDriver;
import lombok.Data;

@Data
public class DataDriverDTO extends DataDriver {
    private String typeDesc;
}
