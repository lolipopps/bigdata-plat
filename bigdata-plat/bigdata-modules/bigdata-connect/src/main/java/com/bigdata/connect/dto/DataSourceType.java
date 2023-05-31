package com.bigdata.connect.dto;


import com.bigdata.connect.constants.DataSourceCalculationMode;
import com.bigdata.connect.constants.DatabaseClassification;
import lombok.Data;

import java.util.List;

@Data
public class DataSourceType {
    public String type;
    public String name;
    public boolean isPlugin = true;
    public DataSourceCalculationMode calculationMode = DataSourceCalculationMode.DIRECT;
    public DatabaseClassification databaseClassification = DatabaseClassification.OTHER;
    public String extraParams;
    public List<String> charset;
    public List<String> targetCharset;
    public boolean isJdbc = false;
    private String keywordPrefix = "";
    private String keywordSuffix = "";
    private String aliasPrefix = "";
    private String aliasSuffix = "";

    public DataSourceType(String type, String name, boolean isPlugin, String extraParams, DataSourceCalculationMode calculationMode, boolean isJdbc) {
        this.type = type;
        this.name = name;
        this.isPlugin = isPlugin;
        this.extraParams = extraParams;
        this.calculationMode = calculationMode;
        this.isJdbc = isJdbc;
    }
}
