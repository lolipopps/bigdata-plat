package com.bigdata.connect.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bigdata.core.base.BigdataBaseEntity;
import io.swagger.annotations.ApiModel;
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
@Table(name = "t_data_driver")
@TableName("t_data_driver")
@ApiModel(value = "生成代码")
public class DataDriver extends BigdataBaseEntity {

    private static final long serialVersionUID = 1L;

    private String name;

    private String type;

    private String driverClass;

    private String desc;


}