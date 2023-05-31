package com.bigdata.connect.provider;

import com.alibaba.druid.pool.DruidDataSource;
import com.bigdata.connect.config.JdbcConfiguration;
import com.bigdata.connect.dto.TableDesc;
import com.bigdata.connect.dto.TableField;
import com.bigdata.connect.entity.DataSource;
import com.bigdata.connect.request.DataSourceRequest;


import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract class Provider {

    abstract public List<String[]> getData(DataSourceRequest DataSourceRequest) throws Exception;

    abstract public List<TableDesc> getTables(DataSourceRequest DataSourceRequest) throws Exception ;

    abstract public String checkStatus(DataSourceRequest DataSourceRequest) throws Exception ;

    public List<String[]> fetchResult(DataSourceRequest DataSourceRequest) throws Exception {
        return null;
    }

    abstract public List<TableField> fetchResultField(DataSourceRequest DataSourceRequest) throws Exception ;

    abstract public Map<String, List> fetchResultAndField(DataSourceRequest DataSourceRequest) throws Exception;

    public void handleDataSource(DataSourceRequest DataSourceRequest, String type) throws Exception{
    }

    public List<String> getSchema(DataSourceRequest DataSourceRequest) throws Exception{
        return null;
    }

    public List<TableField> getTableFields(DataSourceRequest DataSourceRequest) throws Exception {
        return null;
    }

    public String getTablesSql(DataSourceRequest DataSourceRequest) throws Exception{
        return null;
    }

    public String getViewSql(DataSourceRequest DataSourceRequest) throws Exception {
        return null;
    }

    public String getSchemaSql(DataSourceRequest DataSourceRequest) throws Exception{
        return null;
    }

    public Connection getConnection(DataSourceRequest DataSourceRequest) throws Exception{
        return null;
    }

    public JdbcConfiguration setCredential(DataSourceRequest DataSourceRequest, DruidDataSource DataSource) throws Exception{
        return null;
    }

    public Connection getConnectionFromPool(DataSourceRequest DataSourceRequest) throws Exception {
        return null;
    }

    public void addToPool(DataSourceRequest DataSourceRequest) throws PropertyVetoException, SQLException, Exception {
    }

    public void checkConfiguration(DataSource DataSource) throws Exception{}

    public String dsVersion(DataSource DataSource) throws Exception{return "";}
}
