package com.bigdata.quartz.entity;

import com.bigdata.core.base.BigdataBaseEntity;
import com.bigdata.core.common.constant.CommonConstant;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
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
@Table(name = "t_quartz_job")
@TableName("t_quartz_job")
@ApiModel(value = "定时任务")
public class QuartzJob extends BigdataBaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务类名")
    private String jobClassName;

    @ApiModelProperty(value = "cron表达式")
    private String cronExpression;

    @ApiModelProperty(value = "参数")
    private String parameter;

    @ApiModelProperty(value = "备注")
    private String description;

    @ApiModelProperty(value = "状态 0正常 -1停止")
    private Integer status = CommonConstant.STATUS_NORMAL;
}
