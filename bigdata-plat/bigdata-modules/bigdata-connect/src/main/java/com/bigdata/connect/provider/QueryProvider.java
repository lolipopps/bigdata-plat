package com.bigdata.connect.provider;

import com.bigdata.connect.config.Dateformat;
import com.bigdata.connect.config.JdbcConfiguration;
import com.bigdata.connect.constants.db.PgConstants;
import com.bigdata.connect.dto.SQLObj;
import com.bigdata.connect.entity.DataSource;

import com.google.gson.Gson;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/5/17 2:42 下午
 */
public abstract class QueryProvider {

    public abstract Integer transFieldType(String field);

    public abstract String createSQLPreview(String sql, String orderBy);


    public abstract String searchTable(String table);


    public Integer transFieldSize(String type) {
        return 50;
    }


    public abstract String wrapSql(String sql);
   public void setSchema(SQLObj tableObj, DataSource ds) {
        if (ds != null && !tableObj.getTableName().startsWith("(") && !tableObj.getTableName().endsWith(")")) {
            JdbcConfiguration configuration = new Gson().fromJson(ds.getConfiguration(), JdbcConfiguration.class);
            String schema;
            if (ObjectUtils.isNotEmpty(configuration) && StringUtils.isNotBlank((schema = configuration.getSchema()))) {
                schema = String.format(PgConstants.KEYWORD_TABLE, schema);
                tableObj.setTableName(schema + "." + tableObj.getTableName());
            }
        }
    }

    public String convertTableToSql(String tableName, DataSource ds) {
        return "select * from  TABLE_NAME".replace("TABLE_NAME", tableName);
    }

    public String getLogic(String logic) {
        if (logic != null) {
            switch (logic) {
                case "and":
                    return "AND";
                case "or":
                    return "OR";
            }
        }
        return "AND";
    }

    public String sqlForPreview(String table, DataSource ds) {
        return "SELECT * FROM " + table;
    }

    public List<Dateformat> dateformat() {
        return new ArrayList<>();
    }

    public String getTotalCount(boolean isTable, String sql, DataSource ds) {
        return null;
    }
}
