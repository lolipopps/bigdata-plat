package com.bigdata.connect.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataDriverDetails implements Serializable {
    private String id;

    private String deDriverId;

    private String fileName;

    private String version;

    private String driverClass;

    private String transName;

    private Boolean isTransName;

    private static final long serialVersionUID = 1L;
}