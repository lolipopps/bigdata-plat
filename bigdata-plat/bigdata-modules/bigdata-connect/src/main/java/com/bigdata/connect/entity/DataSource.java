package com.bigdata.connect.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bigdata.core.base.BigdataBaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author huyt
 */
@Data
@Accessors(chain = true)
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "t_data_source")
@TableName("t_data_source")
@ApiModel(value = "生成代码")
public class DataSource extends BigdataBaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "名称", required = true)
    private String name;

    @ApiModelProperty("描述")
    private String desc;

    @ApiModelProperty(value = "类型", required = true)
    private String type;

    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty(value = "配置详情", required = true)
    private String configuration;

}