package com.bigdata.connect.constants;

public enum DataSourceCalculationMode {
    DIRECT("DIRECT"),
    SYNC("SYNC"),
    DIRECT_AND_SYNC("DIRECT_AND_SYNC");

    private String  type;
    DataSourceCalculationMode(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }
}
