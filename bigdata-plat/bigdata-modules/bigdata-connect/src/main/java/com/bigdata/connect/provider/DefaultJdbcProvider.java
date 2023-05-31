package com.bigdata.connect.provider;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallFilter;
import com.bigdata.connect.config.JdbcConfiguration;
import com.bigdata.connect.constants.DataSourceTypes;
import com.bigdata.connect.dto.TableDesc;
import com.bigdata.connect.dto.TableField;
import com.bigdata.connect.entity.DataDriver;
import com.bigdata.connect.entity.DataSource;
import com.bigdata.connect.request.DataSourceRequest;
import com.bigdata.core.common.exception.BigdataException;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;

public abstract class DefaultJdbcProvider extends Provider {
    protected Map<String, DruidDataSource> jdbcConnection = new HashMap<>();
    protected ExtendedJdbcClassLoader extendedJdbcClassLoader;
    private Map<String, ExtendedJdbcClassLoader> customJdbcClassLoaders = new HashMap<>();

    static private final String FILE_PATH = "./drivers";
    static private final String THIRDPART_PATH = "./plugins/thirdpart";
    static private final String DEFAULT_PATH = "./plugins/default";
    static private final String CUSTOM_PATH = "./custom-drivers/";

    abstract public boolean isUseDataSourcePool();

    @PostConstruct
    public void init() throws Exception {
        List<String> builtinPlugins = Arrays.asList("maxcompute", "presto", "dm", "mongobi", "kylin", "kingbase");
        String jarPath = FILE_PATH;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (!getType().equalsIgnoreCase("built-in")) {
            if (builtinPlugins.contains(getType())) {
                jarPath = DEFAULT_PATH + "/" + getType() + "Driver";
            } else {
                jarPath = THIRDPART_PATH + "/" + getType() + "Driver";
            }
            while (classLoader.getParent() != null) {
                classLoader = classLoader.getParent();
                if (classLoader.toString().contains("ExtClassLoader")) {
                    break;
                }
            }
        }
        extendedJdbcClassLoader = new ExtendedJdbcClassLoader(new URL[]{new File(jarPath).toURI().toURL()}, classLoader);
        File file = new File(jarPath);
        File[] array = file.listFiles();
        Optional.ofNullable(array).ifPresent(files -> {
            for (File tmp : array) {
                if (tmp.getName().endsWith(".jar")) {
                    try {
                        extendedJdbcClassLoader.addFile(tmp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    abstract public String getType();

    @Override
    public List<String[]> getData(DataSourceRequest dsr) throws Exception {
        List<String[]> list = new LinkedList<>();
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(dsr.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(dsr); Statement stat = getStatement(connection, queryTimeout); ResultSet rs = stat.executeQuery(dsr.getQuery())) {
            list = getDataResult(rs);
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

    public Statement getStatement(Connection connection, int queryTimeout) throws Exception {
        if (connection == null) {
            throw new Exception("Failed to get connection!");
        }
        Statement stat = connection.createStatement();
        try {
            stat.setQueryTimeout(queryTimeout);
        } catch (Exception e) {
        }
        return stat;
    }

    public void exec(DataSourceRequest DataSourceRequest) throws Exception {
        try (Connection connection = getConnectionFromPool(DataSourceRequest); Statement stat = connection.createStatement()) {
            Boolean result = stat.execute(DataSourceRequest.getQuery());
        } catch (SQLException e) {
            new BigdataException(e.toString());
        } catch (Exception e) {
            new BigdataException(e.toString());
        }
    }

    @Override
    public List<TableDesc> getTables(DataSourceRequest DataSourceRequest) throws Exception {
        List<TableDesc> tables = new ArrayList<>();
        String queryStr = getTablesSql(DataSourceRequest);
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection con = getConnectionFromPool(DataSourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
            while (resultSet.next()) {
                tables.add(getTableDesc(DataSourceRequest, resultSet));
            }
        } catch (Exception e) {
            new BigdataException(e.toString());
        }
        String queryView = getViewSql(DataSourceRequest);
        if (queryView != null) {
            try (Connection con = getConnectionFromPool(DataSourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryView)) {
                while (resultSet.next()) {
                    tables.add(getTableDesc(DataSourceRequest, resultSet));
                }
            } catch (Exception e) {
                new BigdataException(e.toString());
            }
        }
        return tables;
    }

    @Override
    public String checkStatus(DataSourceRequest DataSourceRequest) throws Exception {
        String queryStr = getTablesSql(DataSourceRequest);
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection con = getConnection(DataSourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
        } catch (Exception e) {
            new BigdataException(e.getMessage());
        }
        return "Success";
    }

    @Override
    public List<String[]> fetchResult(DataSourceRequest DataSourceRequest) throws Exception {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(DataSourceRequest); Statement stat = getStatement(connection, queryTimeout); ResultSet rs = stat.executeQuery(DataSourceRequest.getQuery())) {
            return getDataResult(rs);
        } catch (Exception e) {
            new BigdataException(e.toString());
        }
        return new ArrayList<>();
    }

    @Override
    public List<TableField> fetchResultField(DataSourceRequest DataSourceRequest) throws Exception {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection connection = getConnectionFromPool(DataSourceRequest); Statement stat = getStatement(connection, queryTimeout); ResultSet rs = stat.executeQuery(DataSourceRequest.getQuery())) {
            return fetchResultField(rs, DataSourceRequest);
        } catch (SQLException e) {
            new BigdataException(e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            new BigdataException("Data source connection exception: " + e.getMessage());
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
        try (Connection connection = getConnectionFromPool(DataSourceRequest); Statement stat = getStatement(connection, queryTimeout); ResultSet rs = stat.executeQuery(DataSourceRequest.getQuery())) {
            fieldList = fetchResultField(rs, DataSourceRequest);
            result.put("fieldList", fieldList);
            dataList = getDataResult(rs);
            result.put("dataList", dataList);
            return result;
        } catch (SQLException e) {
            new BigdataException(e.toString());
        } catch (Exception e) {
            new BigdataException(e.toString());
        }
        return new HashMap<>();
    }

    @Override
    public void handleDataSource(DataSourceRequest DataSourceRequest, String type) throws Exception {
        if (!isUseDataSourcePool()) {
            return;
        }
        DruidDataSource DataSource = null;
        switch (type) {
            case "add":
                checkStatus(DataSourceRequest);
                DataSource = jdbcConnection.get(DataSourceRequest.getDataSource().getId());
                if (DataSource == null) {
                    addToPool(DataSourceRequest);
                }
                break;
            case "edit":
                DataSource = jdbcConnection.get(DataSourceRequest.getDataSource().getId());
                if (DataSource != null) {
                    DataSource.close();
                    jdbcConnection.remove(DataSourceRequest.getDataSource().getId());
                }
                checkStatus(DataSourceRequest);
                addToPool(DataSourceRequest);
                break;
            case "delete":
                DataSource = jdbcConnection.get(DataSourceRequest.getDataSource().getId());
                if (DataSource != null) {
                    DataSource.close();
                    jdbcConnection.remove(DataSourceRequest.getDataSource().getId());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public List<String> getSchema(DataSourceRequest DataSourceRequest) throws Exception {
        List<String> schemas = new ArrayList<>();
        String queryStr = getSchemaSql(DataSourceRequest);
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        int queryTimeout = jdbcConfiguration.getQueryTimeout() > 0 ? jdbcConfiguration.getQueryTimeout() : 0;
        try (Connection con = getConnection(DataSourceRequest); Statement statement = getStatement(con, queryTimeout); ResultSet resultSet = statement.executeQuery(queryStr)) {
            while (resultSet.next()) {
                schemas.add(resultSet.getString(1));
            }
            return schemas;
        } catch (Exception e) {
            new BigdataException(e.toString());
        }
        return new ArrayList<>();
    }

    @Override
    public List<TableField> getTableFields(DataSourceRequest DataSourceRequest) throws Exception {
        List<TableField> list = new LinkedList<>();
        try (Connection connection = getConnectionFromPool(DataSourceRequest)) {

            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet = databaseMetaData.getColumns(null, "%", DataSourceRequest.getTable(), "%");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String database;
                if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.ck.name()) || DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.impala.name())) {
                    database = resultSet.getString("TABLE_SCHEM");
                } else {
                    database = resultSet.getString("TABLE_CAT");
                }
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
            resultSet.close();
        } catch (SQLException e) {
            new BigdataException(e.toString());
        } catch (Exception e) {
            if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase("ds_doris")) {
                DataSourceRequest.setQuery("select * from " + DataSourceRequest.getTable());
                return fetchResultField(DataSourceRequest);
            } else {
                new BigdataException("Data source connection exception: " + e.getMessage());
            }

        }
        return list;
    }

    @Override
    public String getTablesSql(DataSourceRequest DataSourceRequest) throws Exception {
        return "show tables;";
    }

    @Override
    public String getViewSql(DataSourceRequest DataSourceRequest) throws Exception {
        return null;
    }

    @Override
    public String getSchemaSql(DataSourceRequest DataSourceRequest) {
        return null;
    }

    @Override
    public Connection getConnectionFromPool(DataSourceRequest DataSourceRequest) throws Exception {
        if (!isUseDataSourcePool()) {
            return getConnection(DataSourceRequest);
        }
        if (DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.mongo.name()) ||
                DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.impala.name())
                || DataSourceRequest.getDataSource().getType().equalsIgnoreCase(DataSourceTypes.hive.name())) {
            return getConnection(DataSourceRequest);
        }
        DruidDataSource DataSource = jdbcConnection.get(DataSourceRequest.getDataSource().getId());
        if (DataSource == null) {
            handleDataSource(DataSourceRequest, "add");
        }
        DataSource = jdbcConnection.get(DataSourceRequest.getDataSource().getId());
        Connection co = DataSource.getConnection();
        return co;
    }

    @Override
    public void addToPool(DataSourceRequest DataSourceRequest) throws Exception {
        if (!isUseDataSourcePool()) {
            return;
        }
        DruidDataSource druidDataSource = new DruidDataSource();
        JdbcConfiguration jdbcConfiguration = setCredential(DataSourceRequest, druidDataSource);
        druidDataSource.setInitialSize(jdbcConfiguration.getInitialPoolSize());// 初始连接数
        druidDataSource.setMinIdle(jdbcConfiguration.getMinPoolSize()); // 最小连接数
        druidDataSource.setMaxActive(jdbcConfiguration.getMaxPoolSize()); // 最大连接数
        if (DataSourceRequest.getDataSource().getType().equals(DataSourceTypes.mongo.name()) || DataSourceRequest.getDataSource().getType().equals(DataSourceTypes.hive.name()) || DataSourceRequest.getDataSource().getType().equals(DataSourceTypes.impala.name())) {
            WallFilter wallFilter = new WallFilter();
            wallFilter.setDbType(DataSourceTypes.mysql.name());
            druidDataSource.setProxyFilters(Arrays.asList(new Filter[]{wallFilter}));
        }
        druidDataSource.init();
        jdbcConnection.put(DataSourceRequest.getDataSource().getId(), druidDataSource);
    }

    @Override
    public JdbcConfiguration setCredential(DataSourceRequest DataSourceRequest, DruidDataSource DataSource) throws Exception {
        return null;
    }

    public void reloadCustomJdbcClassLoader(DataDriver deDriver) throws Exception {
        if (customJdbcClassLoaders.get(deDriver.getId()) != null) {
            customJdbcClassLoaders.remove(deDriver.getId());
        }
        addCustomJdbcClassLoader(deDriver);
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

        String size = resultSet.getString("COLUMN_SIZE");
        if (size == null) {
            tableField.setFieldSize(1);
        } else {
            tableField.setFieldSize(Integer.valueOf(size));
        }
        return tableField;
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
            field.setFieldSize(metaData.getColumnDisplaySize(j + 1));
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

    private String getDatabase(DataSourceRequest DataSourceRequest) {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        return jdbcConfiguration.getDataBase();
    }

    private List<String[]> getDataResult(ResultSet rs) throws Exception {
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
                        row[j] = rs.getString(j + 1);
                        break;
                }
            }
            list.add(row);
        }
        return list;
    }

    private TableDesc getTableDesc(DataSourceRequest DataSourceRequest, ResultSet resultSet) throws SQLException {
        TableDesc tableDesc = new TableDesc();
        DataSourceTypes DataSourceType = DataSourceTypes.valueOf(DataSourceRequest.getDataSource().getType());
        if (DataSourceType == DataSourceTypes.oracle) {
            tableDesc.setRemark(resultSet.getString(3));
        }
        if (DataSourceType == DataSourceTypes.mysql) {
            tableDesc.setRemark(resultSet.getString(2));
        }
        tableDesc.setName(resultSet.getString(1));
        return tableDesc;
    }


    protected ExtendedJdbcClassLoader getCustomJdbcClassLoader(DataDriver deDriver) throws Exception {
        if (deDriver == null) {
            throw new Exception("Can not found custom Driver");
        }
        ExtendedJdbcClassLoader customJdbcClassLoader = customJdbcClassLoaders.get(deDriver.getId());
        if (customJdbcClassLoader == null) {
            return addCustomJdbcClassLoader(deDriver);
        } else {
            if (StringUtils.isNotEmpty(customJdbcClassLoader.getDriver()) && customJdbcClassLoader.getDriver().equalsIgnoreCase(deDriver.getDriverClass())) {
                return customJdbcClassLoader;
            } else {
                customJdbcClassLoaders.remove(deDriver.getId());
                return addCustomJdbcClassLoader(deDriver);
            }
        }
    }

    private synchronized ExtendedJdbcClassLoader addCustomJdbcClassLoader(DataDriver deDriver) throws Exception {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        while (classLoader.getParent() != null) {
            classLoader = classLoader.getParent();
            if (classLoader.toString().contains("ExtClassLoader")) {
                break;
            }
        }
        ExtendedJdbcClassLoader customJdbcClassLoader = new ExtendedJdbcClassLoader(new URL[]{new File(CUSTOM_PATH + deDriver.getId()).toURI().toURL()}, classLoader);
        customJdbcClassLoader.setDriver(deDriver.getDriverClass());

        File file = new File(CUSTOM_PATH + deDriver.getId());
        File[] array = file.listFiles();
        Optional.ofNullable(array).ifPresent(files -> {
            for (File tmp : array) {
                if (tmp.getName().endsWith(".jar")) {
                    try {
                        customJdbcClassLoader.addFile(tmp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        customJdbcClassLoaders.put(deDriver.getId(), customJdbcClassLoader);
        return customJdbcClassLoader;
    }

    protected boolean isDefaultClassLoader(String customDriver) {
        return StringUtils.isEmpty(customDriver) || customDriver.equalsIgnoreCase("default");
    }

    @Override
    public void checkConfiguration(DataSource DataSource) throws Exception {
        if (StringUtils.isEmpty(DataSource.getConfiguration())) {
            throw new Exception("DataSource configuration is empty");
        }
        try {
            JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSource.getConfiguration(), JdbcConfiguration.class);
            if (jdbcConfiguration.getQueryTimeout() < 0) {
                throw new Exception("Querytimeout cannot be less than zero.");
            }
        } catch (Exception e) {
            throw new Exception("Invalid configuration: " + e.getMessage());
        }
    }


    public String dsVersion(DataSourceRequest DataSourceRequest) throws Exception {
        JdbcConfiguration jdbcConfiguration = new Gson().fromJson(DataSourceRequest.getDataSource().getConfiguration(), JdbcConfiguration.class);
        try (Connection con = getConnectionFromPool(DataSourceRequest)) {
            return String.valueOf(con.getMetaData().getDatabaseMajorVersion());
        } catch (Exception e) {
            new BigdataException(e.getMessage());
        }
        return "";
    }

}
