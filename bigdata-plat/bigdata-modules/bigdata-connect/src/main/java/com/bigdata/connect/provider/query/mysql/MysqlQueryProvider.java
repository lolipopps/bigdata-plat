package com.bigdata.connect.provider.query.mysql;

import com.bigdata.connect.provider.QueryProvider;
import org.springframework.stereotype.Service;



/**
 * @Author gin
 * @Date 2021/5/17 2:43 下午
 */
@Service("mysqlQueryProvider")
public class MysqlQueryProvider extends QueryProvider {

    @Override
    public Integer transFieldType(String field) {
        switch (field) {
            case "CHAR":
            case "VARCHAR":
            case "TEXT":
            case "TINYTEXT":
            case "MEDIUMTEXT":
            case "LONGTEXT":
            case "ENUM":
                return 0;// 文本
            case "DATE":
            case "TIME":
            case "YEAR":
            case "DATETIME":
            case "TIMESTAMP":
                return 1;// 时间
            case "INT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INTEGER":
            case "BIGINT":
            case "LONG": //增加了LONG类型
                return 2;// 整型
            case "FLOAT":
            case "DOUBLE":
            case "DECIMAL":
                return 3;// 浮点
            case "BIT":
            case "TINYINT":
                return 4;// 布尔
            default:
                return 0;
        }
    }

    @Override
    public String createSQLPreview(String sql, String orderBy) {
        return "SELECT * FROM (" + sqlFix(sql) + ") AS tmp " + " LIMIT 0,1000";
    }

    @Override
    public String searchTable(String table) {
        return "SELECT table_name FROM information_schema.TABLES WHERE table_name ='" + table + "'";
    }


    @Override
    public String wrapSql(String sql) {
        sql = sql.trim();
        if (sql.lastIndexOf(";") == (sql.length() - 1)) {
            sql = sql.substring(0, sql.length() - 1);
        }
        String tmpSql = "SELECT * FROM (" + sql + ") AS tmp " + " LIMIT 0";
        return tmpSql;
    }


    private String sqlFix(String sql) {
        sql = sql.trim();
        if (sql.lastIndexOf(";") == (sql.length() - 1)) {
            sql = sql.substring(0, sql.length() - 1);
        }
        return sql;
    }
}
