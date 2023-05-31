package com.bigdata.connect.serviceimpl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.bigdata.connect.config.JdbcConfiguration;
import com.bigdata.connect.config.MysqlConfiguration;
import com.bigdata.connect.constants.DataSourceCalculationMode;
import com.bigdata.connect.constants.DataSourceTypes;
import com.bigdata.connect.constants.RedisConstants;
import com.bigdata.connect.dto.DataSourceDTO;
import com.bigdata.connect.dto.DataSourceType;
import com.bigdata.connect.entity.DataSourceExample;
import com.bigdata.connect.mapper.DataSourceMapper;
import com.bigdata.connect.mapper.ExtDataSourceMapper;
import com.bigdata.connect.provider.Provider;
import com.bigdata.connect.provider.ProviderFactory;
import com.bigdata.connect.request.ApiDefinition;
import com.bigdata.connect.request.DataSourceRequest;
import com.bigdata.connect.request.DataSourceUnionRequest;
import com.bigdata.connect.util.SpringContextUtil;
import com.bigdata.core.common.exception.BigdataException;
import com.bigdata.core.common.i18n.Translator;
import com.bigdata.core.common.utils.BeanUtils;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.utils.SecurityUtil;
import com.bigdata.core.common.utils.ThreadPoolUtil;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.connect.dao.DataSourceDao;
import com.bigdata.connect.entity.DataSource;
import com.bigdata.connect.service.DataSourceService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 生成代码接口实现
 *
 * @author huyt
 */
@Slf4j
@Service
@Transactional
public class DataSourceServiceImpl implements DataSourceService {

    @Autowired
    private DataSourceDao DataSourceDao;

    @Resource
    private Environment env;

    @Autowired
    DataSourceMapper DataSourceMapper;
    @Autowired
    ExtDataSourceMapper extDataSourceMapper;

    @Autowired
    SecurityUtil securityUtil;

    @Override
    public DataSourceDao getRepository() {
        return DataSourceDao;
    }


    public Collection<DataSourceType> types() {
        Collection<DataSourceType> types = new ArrayList<>();
        types.addAll(SpringContextUtil.getApplicationContext().getBeansOfType(DataSourceType.class).values());

        return types;
    }


    @Transactional(rollbackFor = Exception.class)
    public DataSource addDataSource(DataSourceDTO DataSource) throws Exception {
        preCheckDs(DataSource);
        return insert(DataSource);
    }


    public DataSourceDTO insert(DataSourceDTO DataSource) {
        DataSource.setId(UUID.randomUUID().toString());
        DataSource.setUpdateTime(new Date());
        DataSource.setCreateTime(new Date());
        DataSource.setCreateBy(String.valueOf(securityUtil.getCurrUserSimple().getUsername()));
        checkAndUpdateDataSourceStatus(DataSource);
        DataSourceMapper.insertSelective(DataSource);
        handleConnectionPool(DataSource, "add");

        return DataSource;
    }

    public void preCheckDs(DataSourceDTO DataSource) throws Exception {
        if (!types().stream().map(DataSourceType::getType).collect(Collectors.toList()).contains(DataSource.getType())) {
            throw new BigdataException("DataSource type not supported.");
        }
        if (DataSource.isConfigurationEncryption()) {
            DataSource.setConfiguration(new String(java.util.Base64.getDecoder().decode(DataSource.getConfiguration())));
        }
        Provider DataSourceProvider = ProviderFactory.getProvider(DataSource.getType());
        DataSourceProvider.checkConfiguration(DataSource);
        checkName(DataSource.getName(), DataSource.getType(), DataSource.getId());
    }

    public void handleConnectionPool(String DataSourceId, String type) {
        DataSource DataSource = DataSourceMapper.selectByPrimaryKey(DataSourceId);
        if (DataSource == null) {
            return;
        }
        handleConnectionPool(DataSource, type);
    }

    public void handleConnectionPool(DataSource DataSource, String type) {
        ThreadPoolUtil.getPool().execute(() -> {
            try {
                Provider DataSourceProvider = ProviderFactory.getProvider(DataSource.getType());
                DataSourceRequest DataSourceRequest = new DataSourceRequest();
                DataSourceRequest.setDataSource(DataSource);
                DataSourceProvider.handleDataSource(DataSourceRequest, type);
                log.info("Success to {} DataSource connection pool: {}", type, DataSource.getName());
            } catch (Exception e) {
                log.error("Failed to handle DataSource connection pool: " + DataSource.getName(), e);
            }
        });
    }

    public List<DataSourceDTO> getDataSourceList(DataSourceUnionRequest request) throws Exception {
        List<DataSourceDTO> DataSourceDTOS = extDataSourceMapper.queryUnion(request);
        DataSourceDTOS.forEach(this::DataSourceTrans);
        if (StringUtils.isBlank(request.getSort())) {
            DataSourceDTOS.sort((o1, o2) -> {
                int tmp = StringUtils.compareIgnoreCase(o1.getTypeDesc(), o2.getTypeDesc());
                if (tmp == 0) {
                    tmp = StringUtils.compareIgnoreCase(o1.getName(), o2.getName());
                }
                return tmp;
            });
        }
        return DataSourceDTOS;
    }

    private void DataSourceTrans(DataSourceDTO DataSourceDTO) {
        types().forEach(DataSourceType -> {
            if (DataSourceType.getType().equalsIgnoreCase(DataSourceDTO.getType())) {
                DataSourceDTO.setTypeDesc(DataSourceType.getName());
                DataSourceDTO.setCalculationMode(DataSourceType.getCalculationMode());
            }
        });
        if (!DataSourceDTO.getType().equalsIgnoreCase(DataSourceTypes.api.toString())) {
            JdbcConfiguration configuration = new Gson().fromJson(DataSourceDTO.getConfiguration(), JdbcConfiguration.class);
            if (StringUtils.isNotEmpty(configuration.getCustomDriver()) && !configuration.getCustomDriver().equalsIgnoreCase("default")) {
                DataSourceDTO.setCalculationMode(DataSourceCalculationMode.DIRECT);
            }
            JSONObject jsonObject = JSONObject.parseObject(DataSourceDTO.getConfiguration());
            if (jsonObject.getString("queryTimeout") == null) {
                jsonObject.put("queryTimeout", 30);
                DataSourceDTO.setConfiguration(jsonObject.toString());
            }
        }

        if (DataSourceDTO.getType().equalsIgnoreCase(DataSourceTypes.mysql.toString())) {
            MysqlConfiguration mysqlConfiguration = new Gson().fromJson(DataSourceDTO.getConfiguration(), MysqlConfiguration.class);
            DataSourceDTO.setConfiguration(new Gson().toJson(mysqlConfiguration));
        }
        if (DataSourceDTO.getType().equalsIgnoreCase(DataSourceTypes.api.toString())) {
            List<ApiDefinition> apiDefinitionList = new Gson().fromJson(DataSourceDTO.getConfiguration(), new TypeToken<ArrayList<ApiDefinition>>() {
            }.getType());
            List<ApiDefinition> apiDefinitionListWithStatus = new ArrayList<>();
            int success = 0;
            if (StringUtils.isNotEmpty(DataSourceDTO.getStatus())) {
                JsonObject apiItemStatuses = JsonParser.parseString(DataSourceDTO.getStatus()).getAsJsonObject();

                for (int i = 0; i < apiDefinitionList.size(); i++) {
                    String status = null;
                    if (apiItemStatuses.get(apiDefinitionList.get(i).getName()) != null) {
                        status = apiItemStatuses.get(apiDefinitionList.get(i).getName()).getAsString();
                    }
                    apiDefinitionList.get(i).setStatus(status);
                    apiDefinitionList.get(i).setSerialNumber(i);
                    apiDefinitionListWithStatus.add(apiDefinitionList.get(i));
                    if (StringUtils.isNotEmpty(status) && status.equalsIgnoreCase("Success")) {
                        success++;
                    }
                }
            }
            DataSourceDTO.setApiConfiguration(apiDefinitionListWithStatus);
            if (success == apiDefinitionList.size()) {
                DataSourceDTO.setStatus("Success");
            } else {
                if (success > 0 && success < apiDefinitionList.size()) {
                    DataSourceDTO.setStatus("Warning");
                } else {
                    DataSourceDTO.setStatus("Error");
                }
            }
        }
        if (StringUtils.isNotEmpty(DataSourceDTO.getConfiguration())) {
            DataSourceDTO.setConfiguration(new String(java.util.Base64.getEncoder().encode(DataSourceDTO.getConfiguration().getBytes())));
        }
        if (CollectionUtils.isNotEmpty(DataSourceDTO.getApiConfiguration())) {
            String config = new Gson().toJson(DataSourceDTO.getApiConfiguration());
            DataSourceDTO.setApiConfigurationStr(new String(java.util.Base64.getEncoder().encode(config.getBytes())));
            DataSourceDTO.setApiConfiguration(null);
        }
    }



    public Result deleteDataSource(String DataSourceId) throws Exception {
        DataSource DataSource = DataSourceMapper.selectByPrimaryKey(DataSourceId);
        DataSourceMapper.deleteByPrimaryKey(DataSourceId);
        handleConnectionPool(DataSource, "delete");
        return new ResultUtil().setSuccessMsg("success");
    }



    private void handleConnectionPool(String DataSourceId) {
        String cacheType = env.getProperty("spring.cache.type");
        if (cacheType != null && cacheType.equalsIgnoreCase("redis")) {
            handleConnectionPool(DataSourceId, "delete");
            RedisTemplate redisTemplate = SpringContextUtil.getBean("redisTemplate", RedisTemplate.class);
            redisTemplate.convertAndSend(RedisConstants.DS_REDIS_TOPIC, DataSourceId);
        } else {
            handleConnectionPool(DataSourceId, "edit");
        }
    }

    public Result validate(@NotNull DataSourceDTO DataSource) {
        if (DataSource.isConfigurationEncryption()) {
            DataSource.setConfiguration(new String(java.util.Base64.getDecoder().decode(DataSource.getConfiguration())));
        }
        DataSourceDTO DataSourceDTO = new DataSourceDTO();
        BeanUtils.copyBean(DataSourceDTO, DataSource);
        try {
            Provider DataSourceProvider = ProviderFactory.getProvider(DataSource.getType());
            DataSourceProvider.checkConfiguration(DataSource);
            DataSourceRequest DataSourceRequest = new DataSourceRequest();
            DataSourceRequest.setDataSource(DataSource);
            String DataSourceStatus = DataSourceProvider.checkStatus(DataSourceRequest);
            if (DataSource.getType().equalsIgnoreCase("api")) {
                int success = 0;
                List<ApiDefinition> apiDefinitionList = new Gson().fromJson(DataSource.getConfiguration(), new TypeToken<List<ApiDefinition>>() {
                }.getType());
                List<ApiDefinition> apiDefinitionListWithStatus = new ArrayList<>();

                if (StringUtils.isNotEmpty(DataSourceStatus)) {
                    JsonObject apiItemStatuses = JsonParser.parseString(DataSourceStatus).getAsJsonObject();
                    for (ApiDefinition apiDefinition : apiDefinitionList) {
                        String status = apiItemStatuses.get(apiDefinition.getName()).getAsString();
                        apiDefinition.setStatus(status);
                        apiDefinitionListWithStatus.add(apiDefinition);
                        if (StringUtils.isNotEmpty(status) && status.equalsIgnoreCase("Success")) {
                            success++;
                        }
                    }
                }

                DataSourceDTO.setApiConfiguration(apiDefinitionListWithStatus);
                if (success == apiDefinitionList.size()) {
                    DataSource.setStatus(DataSourceStatus);
                    return ResultUtil.success(DataSourceDTO.toString());
                }
                if (success > 0 && success < apiDefinitionList.size()) {
                    return ResultUtil.error(Translator.get("I18N_DS_INVALID_TABLE") + " " + DataSourceDTO);
                }
                return ResultUtil.error(Translator.get("I18N_DS_INVALID") + " " + DataSourceDTO);
            }
            return ResultUtil.success(DataSourceDTO.toString());
        } catch (Exception e) {
            return ResultUtil.error(Translator.get("I18N_DS_INVALID") + ": " + e.getMessage());
        }
    }

    public Result validate(String DataSourceId) {
        DataSource DataSource = DataSourceMapper.selectByPrimaryKey(DataSourceId);
        if (DataSource == null) {
            return ResultUtil.error("Can not find DataSource: " + DataSourceId);
        }
        String DataSourceStatus = null;
        try {
            Provider DataSourceProvider = ProviderFactory.getProvider(DataSource.getType());
            DataSourceRequest DataSourceRequest = new DataSourceRequest();
            DataSourceRequest.setDataSource(DataSource);
            DataSourceStatus = DataSourceProvider.checkStatus(DataSourceRequest);
            if (DataSource.getType().equalsIgnoreCase("api")) {
                List<ApiDefinition> apiDefinitionList = new Gson().fromJson(DataSource.getConfiguration(), new TypeToken<List<ApiDefinition>>() {
                }.getType());
                JsonObject apiItemStatuses = JsonParser.parseString(DataSourceStatus).getAsJsonObject();
                int success = 0;
                for (ApiDefinition apiDefinition : apiDefinitionList) {
                    String status = apiItemStatuses.get(apiDefinition.getName()).getAsString();
                    apiDefinition.setStatus(status);
                    if (status.equalsIgnoreCase("Success")) {
                        success++;
                    }
                }
                if (success == apiDefinitionList.size()) {
                    DataSource.setStatus(DataSourceStatus);
                    return ResultUtil.success(DataSource.toString());
                }
                if (success > 0 && success < apiDefinitionList.size()) {
                    return ResultUtil.error(Translator.get("I18N_DS_INVALID_TABLE") + " " + DataSource);
                }
                return ResultUtil.error(Translator.get("I18N_DS_INVALID") + " " + DataSource);
            }

            return ResultUtil.success("Success");
        } catch (Exception e) {
            DataSourceStatus = "Error";
            return ResultUtil.error(Translator.get("I18N_DS_INVALID") + ": " + e.getMessage());
        } finally {
            DataSource record = new DataSource();
            record.setStatus(DataSourceStatus);
            DataSourceExample example = new DataSourceExample();
            example.createCriteria().andIdEqualTo(DataSource.getId());
            DataSourceMapper.updateByExampleSelective(record, example);
        }
    }

    public List<String> getSchema(DataSourceDTO DataSource) throws Exception {
        if (DataSource.isConfigurationEncryption()) {
            DataSource.setConfiguration(new String(java.util.Base64.getDecoder().decode(DataSource.getConfiguration())));
        }
        Provider DataSourceProvider = ProviderFactory.getProvider(DataSource.getType());
        DataSourceRequest DataSourceRequest = new DataSourceRequest();
        DataSourceRequest.setDataSource(DataSource);
        return DataSourceProvider.getSchema(DataSourceRequest);
    }


    public DataSource get(String id) {
        return DataSourceMapper.selectByPrimaryKey(id);
    }

    public List<DataSource> selectByType(String type) {
        DataSourceExample DataSourceExample = new DataSourceExample();
        DataSourceExample.createCriteria().andTypeEqualTo(type);
        return DataSourceMapper.selectByExampleWithBLOBs(DataSourceExample);
    }

    public void initAllDataSourceConnectionPool() {
        List<DataSource> DataSources = DataSourceMapper.selectByExampleWithBLOBs(new DataSourceExample());
        DataSources.forEach(DataSource -> {
            ThreadPoolUtil.getPool().execute(() -> {
                try {
                    handleConnectionPool(DataSource, "add");
                } catch (Exception e) {
                    log.error("Failed to init DataSource: " + DataSource.getName(), e);
                }
            });
        });
    }

    public void checkName(String DataSourceName, String type, String id) {
        DataSourceExample example = new DataSourceExample();
        DataSourceExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(DataSourceName);
        criteria.andTypeEqualTo(type);
        if (StringUtils.isNotEmpty(id)) {
            criteria.andIdNotEqualTo(id);
        }
        if (CollectionUtils.isNotEmpty(DataSourceMapper.selectByExample(example))) {
            new BigdataException(Translator.get("i18n_ds_name_exists"));
        }
    }



    public List<DataSource> listByType(String type) {
        DataSourceExample example = new DataSourceExample();
        example.createCriteria().andTypeEqualTo(type);
        return DataSourceMapper.selectByExampleWithBLOBs(example);
    }

    public void checkAndUpdateDataSourceStatus(DataSource DataSource) {
        try {
            Provider DataSourceProvider = ProviderFactory.getProvider(DataSource.getType());
            DataSourceRequest DataSourceRequest = new DataSourceRequest();
            DataSourceRequest.setDataSource(DataSource);
            String status = DataSourceProvider.checkStatus(DataSourceRequest);
            DataSource.setStatus(status);
        } catch (Exception e) {
            DataSource.setStatus("Error");
        }
    }


    public DataSourceDTO getDataSourceDetails(String DataSourceId) {
        DataSourceDTO result = extDataSourceMapper.queryDetails(DataSourceId, String.valueOf(securityUtil.getCurrUserSimple().getId()));
        if (result != null) {
            this.DataSourceTrans(result);
        }
        return result;
    }

    public void updateDemoDs() {
        DataSource DataSource = DataSourceMapper.selectByPrimaryKey("76026997-94f9-4a35-96ca-151084638969");
        if (DataSource == null) {
            return;
        }
        MysqlConfiguration mysqlConfiguration = new Gson().fromJson(DataSource.getConfiguration(), MysqlConfiguration.class);
        Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
        Matcher matcher = WITH_SQL_FRAGMENT.matcher(env.getProperty("spring.DataSource.url"));
        if (!matcher.find()) {
            return;
        }
        mysqlConfiguration.setHost(matcher.group(1));
        mysqlConfiguration.setPort(Integer.valueOf(matcher.group(2)));
        mysqlConfiguration.setDataBase(matcher.group(3).split("\\?")[0]);
        mysqlConfiguration.setExtraParams(matcher.group(3).split("\\?")[1]);
        mysqlConfiguration.setUsername(env.getProperty("spring.DataSource.username"));
        mysqlConfiguration.setPassword(env.getProperty("spring.DataSource.password"));
        DataSource.setConfiguration(new Gson().toJson(mysqlConfiguration));
        DataSourceMapper.updateByPrimaryKeyWithBLOBs(DataSource);
    }

    @Override
    public Page<DataSource> findByCondition(DataSource DataSource, SearchVo searchVo, Pageable pageable) {

        return DataSourceDao.findAll(new Specification<DataSource>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<DataSource> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {

                // TODO 可添加你的其他搜索过滤条件 默认已有创建时间过滤
                Path<Date> createTimeField = root.get("createTime");

                List<Predicate> list = new ArrayList<>();

                // 创建时间
                if (StrUtil.isNotBlank(searchVo.getStartDate()) && StrUtil.isNotBlank(searchVo.getEndDate())) {
                    Date start = DateUtil.parse(searchVo.getStartDate());
                    Date end = DateUtil.parse(searchVo.getEndDate());
                    list.add(cb.between(createTimeField, start, DateUtil.endOfDay(end)));
                }

                Predicate[] arr = new Predicate[list.size()];
                cq.where(list.toArray(arr));
                return null;
            }
        }, pageable);
    }

}