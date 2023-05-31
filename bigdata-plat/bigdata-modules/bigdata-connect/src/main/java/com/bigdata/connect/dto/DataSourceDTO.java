package com.bigdata.connect.dto;

import com.bigdata.connect.constants.DataSourceCalculationMode;
import com.bigdata.connect.entity.DataSource;
import com.bigdata.connect.request.ApiDefinition;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-05-18
 * Description:
 */
@Data
public class DataSourceDTO extends DataSource {

    @ApiModelProperty("权限")
    private String privileges;
    private List<ApiDefinition> apiConfiguration;
    private String apiConfigurationStr;
    private String typeDesc;
    private DataSourceCalculationMode calculationMode;
    private boolean isConfigurationEncryption = false;
}
