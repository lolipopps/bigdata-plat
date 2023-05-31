package com.bigdata.connect.request;

import com.bigdata.connect.entity.DataSource;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021-05-18
 * Description:
 */
@Data
public class DataSourceUnionRequest extends DataSource {

    private String userId;

    private String sort;

}
