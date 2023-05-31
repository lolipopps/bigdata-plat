package com.bigdata.connect.provider;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.bigdata.connect.config.*;
import com.bigdata.connect.constants.DataSourceTypes;
import com.bigdata.connect.constants.db.MySQLConstants;
import com.bigdata.connect.dto.TableField;
import com.bigdata.connect.entity.DataDriver;
import com.bigdata.connect.entity.DataSource;
import com.bigdata.connect.mapper.DataDriverMapper;
import com.bigdata.connect.request.DataSourceRequest;
import com.bigdata.core.common.exception.BigdataException;
import com.bigdata.core.common.i18n.Translator;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

@Service("jdbc")
@Slf4j
public class JdbcProvider extends DefaultJdbcProvider {


    @Resource
    private DataDriverMapper dataDriverMapper;

    @Override
    public boolean isUseDataSourcePool() {
        return true;
    }

    @Override
    public String getType() {
        return "built-in";
    }
    /**
     * 增加缓存机制 key 由 'provider_sql_' dsr.DataSource.id dsr.table dsr.query共4部分组成，命中则使用缓存直接返回不再执行sql逻辑
     * @param dsr
     * @return
     * @throws Exception
     */

    /**
     * 这里使用声明式缓存不是很妥当
     * 改为chartViewService中使用编程式缓存
     *
     * @Cacheable( value = JdbcConstants.JDBC_PROVIDER_KEY,
     * key = "'provider_sql_' + #dsr.DataSource.id + '_' + #dsr.table + '_' + #dsr.query",
     * condition = "#dsr.pageSize == null || #dsr.pageSize == 0L"
     * )
     */

    public void exec(DataSourceRequest DataSourceRequest) throws Exception {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(DataSourceRequest); Statement stat = getStatement(connection, queryTimeout)) {
            Boolean result = stat.execute(DataSourceRequest.getQuery());
        } catch (SQLException e) {
            new BigdataException(e.toString());
        } catch (Exception e) {
            new BigdataException(e.toString());
        }
    }


    @Override
    public List<TableField> getTableFields(DataSourceRequest DataSourceRequest) throws Exception {
        if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase("mongo")) {
            DataSourceRequest.setQuery("select * from " + DataSourceRequest.getTable());
            return fetchResultField(DataSourceRequest);
        }
        List<TableField> list = new LinkedList<>();
        try (Connection connection = getConnectionFromPool(DataSourceRequest)) {
            if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase("oracle")) {
                OracleConfiguration oracleConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), OracleConfiguration.class);
                if (isDefaultClassLoader(oracleConfiguration.getCustomDriver())) {
                    Method setRemarksReporting = extendedJdbcClassLoader.loadClass("oracle.jdbc.driver.OracleConnection").getMethod("setRemarksReporting", boolean.class);
                    setRemarksReporting.invoke(((DruidPooledConnection) connection).getConnection(), true);
                }
            }
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String tableNamePattern = DataSourceRequest.getTable();
            if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.mysql.name())) {
                if (databaseMetaData.getDriverMajorVersion() < 8) {
                    tableNamePattern = String.format(MySQLConstants.KEYWORD_TABLE, tableNamePattern);
                }
            }
            String schemaPattern = "%";
            if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.oracle.name())) {
                schemaPattern = databaseMetaData.getUserName();
            }
            ResultSet resultSet = databaseMetaData.getColumns(null, schemaPattern, tableNamePattern, "%");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String database;
                String schema = resultSet.getString("TABLE_SCHEM");
                if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.pg.name()) || DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.ck.name())
                        || DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.impala.name())) {
                    database = resultSet.getString("TABLE_SCHEM");
                } else {
                    database = resultSet.getString("TABLE_CAT");
                }
                if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.pg.name())) {
                    if (tableName.equals(DataSourceRequest.getTable()) && database.equalsIgnoreCase(getDsSchema(DataSourceRequest))) {
                        TableField tableField = getTableFiled(resultSet, DataSourceRequest);
                        list.add(tableField);
                    }
                } else if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.sqlServer.name())) {
                    if (tableName.equals(DataSourceRequest.getTable()) && database.equalsIgnoreCase(getDatabase(DataSourceRequest)) && schema.equalsIgnoreCase(getDsSchema(DataSourceRequest))) {
                        TableField tableField = getTableFiled(resultSet, DataSourceRequest);
                        list.add(tableField);
                    }
                } else {
                    if (database != null) {
                        if (tableName.equals(DataSourceRequest.getTable()) && database.equalsIgnoreCase(getDatabase(DataSourceRequest))) {
                            TableField tableField = getTableFiled(resultSet, DataSourceRequest);
                            list.add(tableField);
                        }
                    } else {
                        if (tableName.equals(DataSourceRequest.getTable())) {
                            TableField tableField = getTableFiled(resultSet, DataSourceRequest);
                            list.add(tableField);
                        }
                    }
                }

            }
            resultSet.close();
        } catch (SQLException e) {
            new BigdataException(e.toString());
        } catch (Exception e) {
            if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase("ds_doris") || DataSourceRequest.getDataSource().getType().equalsIgnoreCase("StarRocks")) {
                DataSourceRequest.setQuery("select * from " + DataSourceRequest.getTable());
                return fetchResultField(DataSourceRequest);
            } else {
                new BigdataException(Translator.get("i18n_DataSource_connect_error") + e.getMessage());
            }

        }
        return list;
    }

    private TableField getTableFiled(ResultSet resultSet, DataSourceRequest DataSourceRequest) throws SQLException {
        TableField tableField = new TableField();
        String colName = resultSet.getString("COLUMN_NAME");
        tableField.setFieldName(colName);
        String remarks = resultSet.getString("REMARKS");
        if (remarks == null || remarks.equals("")) {
            remarks = colName;
        }
        tableField.setRemarks(remarks);
        String dbType = resultSet.getString("TYPE_NAME").toUpperCase();
        tableField.setFieldType(dbType);
        if (dbType.equalsIgnoreCase("LONG")) {
            tableField.setFieldSize(65533);
        }
        if (StringUtils.isNotEmpty(dbType) && dbType.toLowerCase().contains("date") && tableField.getFieldSize() < 50) {
            tableField.setFieldSize(50);
        }

        if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.ck.name())) {
            QueryProvider qp = ProviderFactory.getQueryProvider(DataSourceRequest.getDataSource().getType());
            tableField.setFieldSize(qp.transFieldSize(dbType));
        } else {
            if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.hive.name()) && tableField.getFieldType().equalsIgnoreCase("BOOLEAN")) {
                tableField.setFieldSize(1);
            } else {
                String size = resultSet.getString("COLUMN_SIZE");
                if (size == null) {
                    if (dbType.equals("JSON") && DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.mysql.name())) {
                        tableField.setFieldSize(65535);
                    } else {
                        tableField.setFieldSize(1);
                    }

                } else {
                    tableField.setFieldSize(Integer.valueOf(size));
                }
            }
        }
        if (StringUtils.isNotEmpty(tableField.getFieldType()) && tableField.getFieldType().equalsIgnoreCase("DECIMAL")) {
            tableField.setAccuracy(Integer.valueOf(resultSet.getString("DECIMAL_DIGITS")));
        }
        return tableField;
    }

    private String getDatabase(DataSourceRequest DataSourceRequest) {
        DataSourceTypes DataSourceType = DataSourceTypes.valueOf(DataSourceRequest.getDataSource().getType());
        switch (DataSourceType) {
            case mysql:
            case engine_doris:
            case ds_doris:
            case mariadb:
            case TiDB:
            case StarRocks:
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), MysqlConfiguration.class);
                return mysqlConfiguration.getDataBase();
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), SqlServerConfiguration.class);
                return sqlServerConfiguration.getDataBase();
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), PgConfiguration.class);
                return pgConfiguration.getDataBase();
            default:
                JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
                return jdbcConfiguration.getDataBase();
        }
    }

    private String getDsSchema(DataSourceRequest DataSourceRequest) {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        return jdbcConfiguration.getSchema();
    }

    @Override
    public List<TableField> fetchResultField(DataSourceRequest DataSourceRequest) throws Exception {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(DataSourceRequest); Statement stat = connection.createStatement(); ResultSet rs = stat.executeQuery(DataSourceRequest.getQuery())) {
            return fetchResultField(rs, DataSourceRequest);
        } catch (SQLException e) {
            new BigdataException(e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            new BigdataException(Translator.get("i18n_DataSource_connect_error") + e.getMessage());
        }
        return new ArrayList<>();
    }


    @Override
    public Map<String, List> fetchResultAndField(DataSourceRequest DataSourceRequest) throws Exception {
        Map<String, List> result = new HashMap<>();
        List<String[]> dataList;
        List<TableField> fieldList;
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(DataSourceRequest); Statement stat = connection.createStatement(); ResultSet rs = stat.executeQuery(DataSourceRequest.getQuery())) {
            fieldList = fetchResultField(rs, DataSourceRequest);
            result.put("fieldList", fieldList);
            dataList = getDataResult(rs, DataSourceRequest);
            result.put("dataList", dataList);
            return result;
        } catch (SQLException e) {
            new BigdataException(e.toString());
        } catch (Exception e) {
            new BigdataException(e.toString());
        }
        return new HashMap<>();
    }

    private List<String[]> getDataResult(ResultSet rs, DataSourceRequest DataSourceRequest) throws Exception {
        String charset = null;
        String targetCharset = "UTF-8";
        if (DataSourceRequest != null && DataSourceRequest.getDataSource().getType().equalsIgnoreCase("oracle")) {
            JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
            if (StringUtils.isNotEmpty(jdbcConfiguration.getCharset()) && !jdbcConfiguration.getCharset().equalsIgnoreCase("Default")) {
                charset = jdbcConfiguration.getCharset();
            }
            if (StringUtils.isNotEmpty(jdbcConfiguration.getTargetCharset()) && !jdbcConfiguration.getTargetCharset().equalsIgnoreCase("Default")) {
                targetCharset = jdbcConfiguration.getTargetCharset();
            }
        }
        List<String[]> list = new LinkedList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int j = 0; j < columnCount; j++) {
                int columnType = metaData.getColumnType(j + 1);
                switch (columnType) {
                    case Types.DATE:
                        if (rs.getDate(j + 1) != null) {
                            row[j] = rs.getDate(j + 1).toString();
                        }
                        break;
                    case Types.BOOLEAN:
                        row[j] = rs.getBoolean(j + 1) ? "1" : "0";
                        break;
                    default:
                        if (metaData.getColumnTypeName(j + 1).toLowerCase().equalsIgnoreCase("blob")) {
                            row[j] = rs.getBlob(j + 1) == null ? "" : rs.getBlob(j + 1).toString();
                        } else {
                            if (charset != null && StringUtils.isNotEmpty(rs.getString(j + 1))) {
                                String originStr = new String(rs.getString(j + 1).getBytes(charset), targetCharset);
                                row[j] = new String(originStr.getBytes("UTF-8"), "UTF-8");
                            } else {
                                row[j] = rs.getString(j + 1);
                            }
                        }

                        break;
                }
            }
            list.add(row);
        }
        return list;
    }

    private List<TableField> fetchResultField(ResultSet rs, DataSourceRequest DataSourceRequest) throws Exception {
        List<TableField> fieldList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int j = 0; j < columnCount; j++) {
            String f = metaData.getColumnName(j + 1);
            String l = StringUtils.isNotEmpty(metaData.getColumnLabel(j + 1)) ? metaData.getColumnLabel(j + 1) : f;
            String t = metaData.getColumnTypeName(j + 1);
            if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.hive.name()) && l.contains(".")) {
                l = l.split("\\.")[1];
            }
            TableField field = new TableField();
            field.setFieldName(l);
            field.setRemarks(l);
            field.setFieldType(t);

            if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.ck.name())) {
                QueryProvider qp = ProviderFactory.getQueryProvider(DataSourceRequest.getDataSource().getType());
                field.setFieldSize(qp.transFieldSize(t));
            } else {
                field.setFieldSize(metaData.getColumnDisplaySize(j + 1));
            }
            if (t.equalsIgnoreCase("LONG")) {
                field.setFieldSize(65533);
            } //oracle LONG
            if (StringUtils.isNotEmpty(t) && t.toLowerCase().contains("date") && field.getFieldSize() < 50) {
                field.setFieldSize(50);
            }
            fieldList.add(field);
        }
        return fieldList;
    }

    @Override
    public List<String[]> getData(DataSourceRequest dsr) throws Exception {
        List<String[]> list = new LinkedList<>();
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(dsr.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(dsr); Statement stat = getStatement(connection, queryTimeout); ResultSet rs = stat.executeQuery(dsr.getQuery())) {
            list = getDataResult(rs, dsr);
            if (dsr.isPageable() && (dsr.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.sqlServer.name()) || dsr.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.db2.name()))) {
                Integer realSize = dsr.getPage() * dsr.getPageSize() < list.size() ? dsr.getPage() * dsr.getPageSize() : list.size();
                list = list.subList((dsr.getPage() - 1) * dsr.getPageSize(), realSize);
            }

        } catch (SQLException e) {
            new BigdataException("SQL ERROR" + e.getMessage());
        } catch (Exception e) {
            new BigdataException("Data source connection exception: " + e.getMessage());
        }
        return list;
    }

    @Override
    public String checkStatus(DataSourceRequest DataSourceRequest) throws Exception {
        String queryStr = getTablesSql(DataSourceRequest);
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection con = getConnection(DataSourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
        } catch (Exception e) {
            log.error("DataSource is invalid: " + DataSourceRequest.getDataSource().getName(), e);
         new BigdataException(e.getMessage());
        }
        return "Success";
    }

    @Override
    public Connection getConnection(DataSourceRequest DataSourceRequest) throws Exception {
        String username = null;
        String password = null;
        String defaultDriver = null;
        String jdbcurl = null;
        String customDriver = null;
        DataSourceTypes DataSourceType = DataSourceTypes.valueOf(DataSourceRequest.getDataSource().getType());
        Properties props = new Properties();
        DataDriver deDriver = null;
        switch (DataSourceType) {
            case mysql:
            case mariadb:
            case engine_doris:
            case engine_mysql:
            case ds_doris:
            case TiDB:
            case StarRocks:
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), MysqlConfiguration.class);
                username = mysqlConfiguration.getUsername();
                password = mysqlConfiguration.getPassword();
                defaultDriver = "com.mysql.jdbc.Driver";
                jdbcurl = mysqlConfiguration.getJdbc();
                customDriver = mysqlConfiguration.getCustomDriver();
                break;
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), SqlServerConfiguration.class);
                username = sqlServerConfiguration.getUsername();
                password = sqlServerConfiguration.getPassword();
                defaultDriver = sqlServerConfiguration.getDriver();
                customDriver = sqlServerConfiguration.getCustomDriver();
                jdbcurl = sqlServerConfiguration.getJdbc();
                break;
            case oracle:
                OracleConfiguration oracleConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), OracleConfiguration.class);
                username = oracleConfiguration.getUsername();
                password = oracleConfiguration.getPassword();
                defaultDriver = oracleConfiguration.getDriver();
                customDriver = oracleConfiguration.getCustomDriver();
                jdbcurl = oracleConfiguration.getJdbc();
                props.put("oracle.net.CONNECT_TIMEOUT", "5000");
                break;
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), PgConfiguration.class);
                username = pgConfiguration.getUsername();
                password = pgConfiguration.getPassword();
                defaultDriver = pgConfiguration.getDriver();
                customDriver = pgConfiguration.getCustomDriver();
                jdbcurl = pgConfiguration.getJdbc();
                break;
            case ck:
                CHConfiguration chConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), CHConfiguration.class);
                username = chConfiguration.getUsername();
                password = chConfiguration.getPassword();
                defaultDriver = chConfiguration.getDriver();
                customDriver = chConfiguration.getCustomDriver();
                jdbcurl = chConfiguration.getJdbc();
                break;
            case mongo:
                MongodbConfiguration mongodbConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), MongodbConfiguration.class);
                username = mongodbConfiguration.getUsername();
                password = mongodbConfiguration.getPassword();
                defaultDriver = mongodbConfiguration.getDriver();
                customDriver = mongodbConfiguration.getCustomDriver();
                jdbcurl = mongodbConfiguration.getJdbc(DataSourceRequest.getDataSource().getId());
                break;
            case redshift:
                RedshiftConfiguration redshiftConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), RedshiftConfiguration.class);
                username = redshiftConfiguration.getUsername();
                password = redshiftConfiguration.getPassword();
                defaultDriver = redshiftConfiguration.getDriver();
                customDriver = redshiftConfiguration.getCustomDriver();
                jdbcurl = redshiftConfiguration.getJdbc();
                break;
            case hive:
                HiveConfiguration hiveConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), HiveConfiguration.class);
                defaultDriver = hiveConfiguration.getDriver();
                customDriver = hiveConfiguration.getCustomDriver();
                jdbcurl = hiveConfiguration.getJdbc();

                if (StringUtils.isNotEmpty(hiveConfiguration.getAuthMethod()) && hiveConfiguration.getAuthMethod().equalsIgnoreCase("kerberos")) {
                    System.setProperty("java.security.krb5.conf", "./conf/krb5.conf");
                    ExtendedJdbcClassLoader classLoader;
                    if (isDefaultClassLoader(customDriver)) {
                        classLoader = extendedJdbcClassLoader;
                    } else {
                        deDriver = dataDriverMapper.selectByPrimaryKey(customDriver);
                        classLoader = getCustomJdbcClassLoader(deDriver);
                    }
                    Class<?> ConfigurationClass = classLoader.loadClass("org.apache.hadoop.conf.Configuration");
                    Method set = ConfigurationClass.getMethod("set", String.class, String.class);
                    Object obj = ConfigurationClass.newInstance();
                    set.invoke(obj, "hadoop.security.authentication", "Kerberos");

                    Class<?> UserGroupInformationClass = classLoader.loadClass("org.apache.hadoop.security.UserGroupInformation");
                    Method setConfiguration = UserGroupInformationClass.getMethod("setConfiguration", ConfigurationClass);
                    Method loginUserFromKeytab = UserGroupInformationClass.getMethod("loginUserFromKeytab", String.class, String.class);
                    setConfiguration.invoke(null, obj);
                    loginUserFromKeytab.invoke(null, hiveConfiguration.getUsername(), "./conf/" + hiveConfiguration.getPassword());
                } else {
                    username = hiveConfiguration.getUsername();
                    password = hiveConfiguration.getPassword();
                }
                break;
            case impala:
                ImpalaConfiguration impalaConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), ImpalaConfiguration.class);
                username = impalaConfiguration.getUsername();
                password = impalaConfiguration.getPassword();
                defaultDriver = impalaConfiguration.getDriver();
                customDriver = impalaConfiguration.getCustomDriver();
                jdbcurl = impalaConfiguration.getJdbc();
                break;
            case db2:
                Db2Configuration db2Configuration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), Db2Configuration.class);
                username = db2Configuration.getUsername();
                password = db2Configuration.getPassword();
                defaultDriver = db2Configuration.getDriver();
                customDriver = db2Configuration.getCustomDriver();
                jdbcurl = db2Configuration.getJdbc();
                break;
            default:
                break;
        }

        if (StringUtils.isNotBlank(username)) {
            props.setProperty("user", username);
            if (StringUtils.isNotBlank(password)) {
                props.setProperty("password", password);
            }
        }

        Connection conn;
        String driverClassName;
        ExtendedJdbcClassLoader jdbcClassLoader;
        if (isDefaultClassLoader(customDriver)) {
            driverClassName = defaultDriver;
            jdbcClassLoader = extendedJdbcClassLoader;
        } else {
            if (deDriver == null) {
                deDriver = dataDriverMapper.selectByPrimaryKey(customDriver);
            }
            driverClassName = deDriver.getDriverClass();
            jdbcClassLoader = getCustomJdbcClassLoader(deDriver);
        }

        Driver driverClass = (Driver) jdbcClassLoader.loadClass(driverClassName).newInstance();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(jdbcClassLoader);
            conn = driverClass.connect(jdbcurl, props);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return conn;
    }


    @Override
    public JdbcConfiguration setCredential(DataSourceRequest DataSourceRequest, DruidDataSource DataSource) throws Exception {
        DataSourceTypes DataSourceType = DataSourceTypes.valueOf(DataSourceRequest.getDataSource().getType());
        JdbcConfiguration jdbcConfiguration = new JdbcConfiguration();
        switch (DataSourceType) {
            case mysql:
            case mariadb:
            case engine_mysql:
            case engine_doris:
            case ds_doris:
            case TiDB:
            case StarRocks:
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), MysqlConfiguration.class);
                DataSource.setUrl(mysqlConfiguration.getJdbc());
                DataSource.setDriverClassName("com.mysql.jdbc.Driver");
                DataSource.setValidationQuery("select 1");
                jdbcConfiguration = mysqlConfiguration;
                break;
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), SqlServerConfiguration.class);
                DataSource.setDriverClassName(sqlServerConfiguration.getDriver());
                DataSource.setUrl(sqlServerConfiguration.getJdbc());
                DataSource.setValidationQuery("select 1");
                jdbcConfiguration = sqlServerConfiguration;
                break;
            case oracle:
                OracleConfiguration oracleConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), OracleConfiguration.class);
                DataSource.setDriverClassName(oracleConfiguration.getDriver());
                DataSource.setUrl(oracleConfiguration.getJdbc());
                DataSource.setValidationQuery("select 1 from dual");
                jdbcConfiguration = oracleConfiguration;
                break;
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), PgConfiguration.class);
                DataSource.setDriverClassName(pgConfiguration.getDriver());
                DataSource.setUrl(pgConfiguration.getJdbc());
                jdbcConfiguration = pgConfiguration;
                break;
            case ck:
                CHConfiguration chConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), CHConfiguration.class);
                DataSource.setDriverClassName(chConfiguration.getDriver());
                DataSource.setUrl(chConfiguration.getJdbc());
                jdbcConfiguration = chConfiguration;
                break;
            case mongo:
                MongodbConfiguration mongodbConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), MongodbConfiguration.class);
                DataSource.setDriverClassName(mongodbConfiguration.getDriver());
                DataSource.setUrl(mongodbConfiguration.getJdbc(DataSourceRequest.getDataSource().getId()));
                jdbcConfiguration = mongodbConfiguration;
                break;
            case redshift:
                RedshiftConfiguration redshiftConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), RedshiftConfiguration.class);
                DataSource.setPassword(redshiftConfiguration.getPassword());
                DataSource.setDriverClassName(redshiftConfiguration.getDriver());
                DataSource.setUrl(redshiftConfiguration.getJdbc());
                jdbcConfiguration = redshiftConfiguration;
                break;
            case hive:
                HiveConfiguration hiveConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), HiveConfiguration.class);
                DataSource.setPassword(hiveConfiguration.getPassword());
                DataSource.setDriverClassName(hiveConfiguration.getDriver());
                DataSource.setUrl(hiveConfiguration.getJdbc());
                jdbcConfiguration = hiveConfiguration;
                break;
            case impala:
                ImpalaConfiguration impalaConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), ImpalaConfiguration.class);
                DataSource.setPassword(impalaConfiguration.getPassword());
                DataSource.setDriverClassName(impalaConfiguration.getDriver());
                DataSource.setUrl(impalaConfiguration.getJdbc());
                jdbcConfiguration = impalaConfiguration;
                break;
            case db2:
                Db2Configuration db2Configuration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), Db2Configuration.class);
                DataSource.setPassword(db2Configuration.getPassword());
                DataSource.setDriverClassName(db2Configuration.getDriver());
                DataSource.setUrl(db2Configuration.getJdbc());
                jdbcConfiguration = db2Configuration;
            default:
                break;
        }

        DataSource.setUsername(jdbcConfiguration.getUsername());

        ExtendedJdbcClassLoader classLoader;
        if (isDefaultClassLoader(jdbcConfiguration.getCustomDriver())) {
            classLoader = extendedJdbcClassLoader;
        } else {
            DataDriver deDriver = dataDriverMapper.selectByPrimaryKey(jdbcConfiguration.getCustomDriver());
            classLoader = getCustomJdbcClassLoader(deDriver);
        }
        DataSource.setDriverClassLoader(classLoader);
        DataSource.setPassword(jdbcConfiguration.getPassword());

        return jdbcConfiguration;
    }

    @Override
    public String getTablesSql(DataSourceRequest DataSourceRequest) throws Exception {
        DataSourceTypes DataSourceType = DataSourceTypes.valueOf(DataSourceRequest.getDataSource().getType());
        switch (DataSourceType) {
            case mysql:
            case engine_mysql:
            case mariadb:
            case TiDB:
                JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
                return String.format("SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' ;", jdbcConfiguration.getDataBase());
            case engine_doris:
            case ds_doris:
            case StarRocks:
            case hive:
            case impala:
                return "show tables";
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), SqlServerConfiguration.class);
                if (StringUtils.isEmpty(sqlServerConfiguration.getSchema())) {
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT TABLE_NAME FROM \"DATABASE\".INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = 'DS_SCHEMA' ;"
                        .replace("DATABASE", sqlServerConfiguration.getDataBase())
                        .replace("DS_SCHEMA", sqlServerConfiguration.getSchema());
            case oracle:
                OracleConfiguration oracleConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), OracleConfiguration.class);
                if (StringUtils.isEmpty(oracleConfiguration.getSchema())) {
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "select table_name, owner, comments from all_tab_comments where owner='OWNER' AND table_type = 'TABLE' AND table_name in (select table_name from all_tables where owner='OWNER')".replaceAll("OWNER", oracleConfiguration.getSchema());
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), PgConfiguration.class);
                if (StringUtils.isEmpty(pgConfiguration.getSchema())) {
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT tablename FROM  pg_tables WHERE  schemaname='SCHEMA' ;".replace("SCHEMA", pgConfiguration.getSchema());
            case ck:
                CHConfiguration chConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), CHConfiguration.class);
                return "SELECT name FROM system.tables where database='DATABASE';".replace("DATABASE", chConfiguration.getDataBase());
            case redshift:
                RedshiftConfiguration redshiftConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), RedshiftConfiguration.class);
                if (StringUtils.isEmpty(redshiftConfiguration.getSchema())) {
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT tablename FROM  pg_tables WHERE  schemaname='SCHEMA' ;".replace("SCHEMA", redshiftConfiguration.getSchema());
            case db2:
                Db2Configuration db2Configuration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), Db2Configuration.class);
                if (StringUtils.isEmpty(db2Configuration.getSchema())) {
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "select TABNAME from syscat.tables  WHERE TABSCHEMA ='DE_SCHEMA' AND \"TYPE\" = 'T'".replace("DE_SCHEMA", db2Configuration.getSchema());
            default:
                return "show tables;";
        }
    }

    @Override
    public String getViewSql(DataSourceRequest DataSourceRequest) throws Exception {
        DataSourceTypes DataSourceType = DataSourceTypes.valueOf(DataSourceRequest.getDataSource().getType());
        switch (DataSourceType) {
            case mysql:
            case mariadb:
            case engine_doris:
            case engine_mysql:
            case ds_doris:
            case ck:
            case TiDB:
            case StarRocks:
                return null;
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), SqlServerConfiguration.class);
                if (StringUtils.isEmpty(sqlServerConfiguration.getSchema())) {
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT TABLE_NAME FROM \"DATABASE\".INFORMATION_SCHEMA.VIEWS WHERE  TABLE_SCHEMA = 'DS_SCHEMA' ;"
                        .replace("DATABASE", sqlServerConfiguration.getDataBase())
                        .replace("DS_SCHEMA", sqlServerConfiguration.getSchema());
            case oracle:
                OracleConfiguration oracleConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), OracleConfiguration.class);
                if (StringUtils.isEmpty(oracleConfiguration.getSchema())) {
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "select table_name, owner, comments from all_tab_comments where owner='" + oracleConfiguration.getSchema() + "' AND table_type = 'VIEW'";
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), PgConfiguration.class);
                if (StringUtils.isEmpty(pgConfiguration.getSchema())) {
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT viewname FROM  pg_views WHERE schemaname='SCHEMA' ;".replace("SCHEMA", pgConfiguration.getSchema());
            case redshift:
                RedshiftConfiguration redshiftConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), RedshiftConfiguration.class);
                if (StringUtils.isEmpty(redshiftConfiguration.getSchema())) {
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "SELECT viewname FROM  pg_views WHERE schemaname='SCHEMA' ;".replace("SCHEMA", redshiftConfiguration.getSchema());

            case db2:
                Db2Configuration db2Configuration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), Db2Configuration.class);
                if (StringUtils.isEmpty(db2Configuration.getSchema())) {
                    throw new Exception(Translator.get("i18n_schema_is_empty"));
                }
                return "select TABNAME from syscat.tables  WHERE TABSCHEMA ='DE_SCHEMA' AND \"TYPE\" = 'V'".replace("DE_SCHEMA", db2Configuration.getSchema());

            default:
                return null;
        }
    }

    @Override
    public String getSchemaSql(DataSourceRequest DataSourceRequest) {
        DataSourceTypes DataSourceType = DataSourceTypes.valueOf(DataSourceRequest.getDataSource().getType());
        Db2Configuration db2Configuration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), Db2Configuration.class);
        switch (DataSourceType) {
            case oracle:
                return "select * from all_users";
            case sqlServer:
                return "select name from sys.schemas;";
            case db2:
                return "select SCHEMANAME from syscat.SCHEMATA   WHERE \"DEFINER\" ='USER'".replace("USER", db2Configuration.getUsername().toUpperCase());
            case pg:
                return "SELECT nspname FROM pg_namespace;";
            case redshift:
                return "SELECT nspname FROM pg_namespace;";
            default:
                return "show tables;";
        }
    }

    @Override
    public void checkConfiguration(DataSource dataSource) throws Exception {
        if (StringUtils.isEmpty(dataSource.getConfiguration())) {
            throw new Exception("DataSource configuration is empty");
        }
        try {
            JdbcConfiguration jdbcConfiguration = new Gson().fromJson(dataSource.getConfiguration(), JdbcConfiguration.class);
            if (jdbcConfiguration.getQueryTimeout() < 0) {
                throw new Exception("Querytimeout cannot be less than zero.");
            }
        } catch (Exception e) {
            throw new Exception("Invalid configuration: " + e.getMessage());
        }

        DataSourceTypes DataSourceType = DataSourceTypes.valueOf(dataSource.getType());
        switch (DataSourceType) {
            case mysql:
            case mariadb:
            case engine_doris:
            case engine_mysql:
            case ds_doris:
            case TiDB:
            case StarRocks:
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(dataSource.getConfiguration(), MysqlConfiguration.class);
                mysqlConfiguration.getJdbc();
                break;
            case redshift:
                RedshiftConfiguration redshiftConfiguration = new Gson().fromJson(dataSource.getConfiguration(), RedshiftConfiguration.class);
                if(redshiftConfiguration.getDataBase().length() > 64 || redshiftConfiguration.getDataBase().length() < 1){
                    throw new Exception("Invalid database name");
                }
                if(!redshiftConfiguration.getDataBase().matches("\"^[a-z][a-z0-9_+.@-]*$\"")){
                    throw new Exception("Invalid database name");
                }
                break;
            default:
                break;
        }
    }


}
