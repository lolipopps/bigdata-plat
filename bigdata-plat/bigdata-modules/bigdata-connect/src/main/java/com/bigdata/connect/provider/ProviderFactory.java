package com.bigdata.connect.provider;

 
import com.bigdata.connect.constants.DataSourceTypes;
import com.bigdata.connect.dto.DataSourceType;
import com.bigdata.connect.util.SpringContextUtil;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ProviderFactory implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext ctx) {
        this.context =  ctx;
        for(final DataSourceTypes d: DataSourceTypes.values()) {
            final ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
            if(d.isDataSource()){
                DataSourceType DataSourceType = new DataSourceType(d.getType(), d.getName(), false, d.getExtraParams(), d.getCalculationMode(), d.isJdbc());
                if(DataSourceType.getType().equalsIgnoreCase("oracle")){
                    DataSourceType.setCharset(d.getCharset());
                    DataSourceType.setTargetCharset(d.getTargetCharset());
                }
                DataSourceType.setKeywordSuffix(d.getKeywordSuffix());
                DataSourceType.setDatabaseClassification(d.getDatabaseClassification());
                DataSourceType.setKeywordPrefix(d.getKeywordPrefix());
                DataSourceType.setAliasSuffix(d.getAliasSuffix());
                DataSourceType.setAliasPrefix(d.getAliasPrefix());
                beanFactory.registerSingleton(d.getType(), DataSourceType);
            }
        }
    }


    public static Provider getProvider(String type) {
        if(type.equalsIgnoreCase(DataSourceTypes.engine_doris.toString()) || type.equalsIgnoreCase(DataSourceTypes.engine_mysql.toString())){
            return context.getBean("jdbc", Provider.class);
        }

        Map<String, DataSourceType> DataSourceTypeMap = SpringContextUtil.getApplicationContext().getBeansOfType((DataSourceType.class));
        if(DataSourceTypeMap.keySet().contains(type)){
            DataSourceTypes DataSourceType = DataSourceTypes.valueOf(type);
            switch (DataSourceType) {
                case es:
                    return context.getBean("esProviders", Provider.class);
                case api:
                    return context.getBean("apiProvider", Provider.class);
                default:
                    return context.getBean("jdbc", Provider.class);
            }
        }

        return SpringContextUtil.getApplicationContext().getBean(type + "DsProvider", Provider.class);

    }

    public static QueryProvider getQueryProvider(String type) {
        switch (type) {
            case "mysql":
            case "mariadb":
                return context.getBean("mysqlQueryProvider", QueryProvider.class);
            case "ds_doris":
            case "TiDB":
            case "StarRocks":
                return context.getBean("dorisQueryProvider", QueryProvider.class);
            default:
                return SpringContextUtil.getApplicationContext().getBean(type + "QueryProvider", QueryProvider.class);
        }

    }

    public static DDLProvider getDDLProvider(String type) {
        DataSourceTypes DataSourceType = DataSourceTypes.valueOf(type);
        switch (DataSourceType) {
            case engine_doris:
                return context.getBean("dorisEngineDDL", DDLProvider.class);
            case engine_mysql:
                return context.getBean("mysqlEngineDDL", DDLProvider.class);
            default:
                return context.getBean("dorisEngineDDL", DDLProvider.class);
        }
    }

}
