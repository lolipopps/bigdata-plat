package com.bigdata.connect.provider.query.hive;

import com.alibaba.fastjson.JSONArray;

import com.bigdata.connect.provider.QueryProvider;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/5/17 2:43 下午
 */
@Service("hiveQueryProvider")
public class HiveQueryProvider extends QueryProvider {

    @Override
    public Integer transFieldType(String field) {
        field = field.toLowerCase();
        switch (field) {
            case "varchar":
            case "string":
            case "char":
                return 0;// 文本
            case "timestamp":
            case "date":
                return 1;// 时间
            case "tinyint":
            case "smallint":
            case "int":
            case "bigint":
                return 2;// 整型
            case "decimal":
            case "float":
            case "double":
                return 3;// 浮点
            case "boolean":
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
        if (sql.lastIndexOf(";") == (sql.length() - 1)) {
            sql = sql.substring(0, sql.length() - 1);
        }
        return sql;
    }
}
