package com.bigdata.file.entity;

import com.bigdata.core.base.BigdataBaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Bigdata
 */
@Data
@Accessors(chain = true)
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "t_file")
@TableName("t_file")
@ApiModel(value = "文件")
public class File extends BigdataBaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "原文件名")
    private String title;

    @ApiModelProperty(value = "存储文件名")
    private String fKey;

    @ApiModelProperty(value = "大小")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long size;

    @ApiModelProperty(value = "文件类型")
    private String type;

    @ApiModelProperty(value = "路径")
    private String url;

    @ApiModelProperty(value = "存储位置 0本地 1七牛 2阿里 3腾讯 4MinIO")
    private Integer location;

    @ApiModelProperty(value = "类别id")
    private String categoryId;

    @ApiModelProperty(value = "收藏")
    private Boolean isCollect;

    @Transient
    @TableField(exist = false)
    @ApiModelProperty(value = "上传者用户名")
    private String nickname;

    @Transient
    @TableField(exist = false)
    @ApiModelProperty(value = "剩余删除时间")
    private String restDelTime;
}