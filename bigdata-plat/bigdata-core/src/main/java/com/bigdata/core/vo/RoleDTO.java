package com.bigdata.core.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Bigdata
 */
@Data
@Accessors(chain = true)
public class RoleDTO {

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "角色名 以ROLE_开头")
    private String name;

    @ApiModelProperty(value = "备注")
    private String description;
}
