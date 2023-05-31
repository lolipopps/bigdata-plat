package com.bigdata.connect.provider.query.doris;

import com.bigdata.connect.entity.DataSource;
import com.bigdata.connect.provider.query.mysql.MysqlQueryProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;


@Service("dorisQueryProvider")
public class DorisQueryProvider extends MysqlQueryProvider {


    public String getTotalCount(boolean isTable, String sql, DataSource ds) {
        return null;
    }
}
