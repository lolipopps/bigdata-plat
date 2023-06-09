package com.bigdata.core.entity;

import com.bigdata.core.base.BigdataBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Bigdata
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "t_setting")
@TableName("t_setting")
@ApiModel(value = "配置")
@NoArgsConstructor
public class Setting extends BigdataBaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "配置值value")
    private String value;

    public Setting(String id) {

        super.setId(id);
    }
}