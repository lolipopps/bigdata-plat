package com.bigdata.connect.serviceimpl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.bigdata.connect.dto.DataDriverDTO;
import com.bigdata.connect.entity.DataDriverDetails;
import com.bigdata.connect.entity.DataDriverDetailsExample;
import com.bigdata.connect.mapper.DataDriverDetailsMapper;
import com.bigdata.connect.mapper.DataDriverMapper;
import com.bigdata.connect.provider.DefaultJdbcProvider;
import com.bigdata.connect.provider.ProviderFactory;
import com.bigdata.connect.service.DataSourceService;
import com.bigdata.connect.util.DeFileUtils;
import com.bigdata.core.common.i18n.Translator;
import com.bigdata.core.common.utils.BeanUtils;
import com.bigdata.core.common.utils.Md5Utils;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.connect.dao.DataDriverDao;
import com.bigdata.connect.entity.DataDriver;
import com.bigdata.connect.service.DataDriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.persistence.criteria.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 生成代码接口实现
 *
 * @author huyt
 */
@Slf4j
@Service
@Transactional
public class DataDriverServiceImpl implements DataDriverService {

    @Autowired
    private DataDriverDao dataDriverDao;

    private static final String DRIVER_PATH = "./custom-drivers/";

    @Resource
    private DataDriverMapper deDriverMapper;
    @Resource
    private DataDriverDetailsMapper deDriverDetailsMapper;

    @Autowired
    DataSourceService dataSourceService;

    @Override
    public DataDriverDao getRepository() {
        return dataDriverDao;
    }

    @Override
    public Page<DataDriver> findByCondition(DataDriver dataDriver, SearchVo searchVo, Pageable pageable) {

        return dataDriverDao.findAll(new Specification<DataDriver>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<DataDriver> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {

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

    public List<DataDriverDetails> listDriverDetails(String driverId) {
        DataDriverDetailsExample example = new DataDriverDetailsExample();
        example.createCriteria().andDeDriverIdEqualTo(driverId);
        return deDriverDetailsMapper.selectByExample(example);
    }

    public List<DataDriverDTO> list() throws Exception {
        List<DataDriverDTO> driverDTOS = new ArrayList<>();
        deDriverMapper.selectByExample(null).forEach(deDriver -> {
            DataDriverDTO driverDTO = new DataDriverDTO();
            BeanUtils.copyBean(driverDTO, deDriver);
            dataSourceService.types().forEach(dataSourceType -> {
                if (dataSourceType.getType().equalsIgnoreCase(deDriver.getType())) {
                    driverDTO.setTypeDesc(dataSourceType.getName());
                }

            });
            driverDTOS.add(driverDTO);
        });
        return driverDTOS;
    }

    public void deleteDriverFile(String driverFileId) throws Exception {
        DataDriverDetails deDriverDetails = deDriverDetailsMapper.selectByPrimaryKey(driverFileId);
        DataDriver deDriver = deDriverMapper.selectByPrimaryKey(deDriverDetails.getDeDriverId());
        if (deDriver == null) {
            throw new Exception(Translator.get("I18N_DRIVER_NOT_FOUND"));
        }
        if (deDriverDetails.getIsTransName() == null || !deDriverDetails.getIsTransName()) {
            DeFileUtils.deleteFile(DRIVER_PATH + deDriverDetails.getDeDriverId() + "/" + deDriverDetails.getFileName());
        } else {
            DeFileUtils.deleteFile(DRIVER_PATH + deDriverDetails.getDeDriverId() + "/" + deDriverDetails.getTransName());
        }
        log.info("删除驱动成功: " + deDriverDetails);
        deDriverDetailsMapper.deleteByPrimaryKey(driverFileId);
        DefaultJdbcProvider defaultJdbcProvider = (DefaultJdbcProvider) ProviderFactory.getProvider(deDriver.getType());
        defaultJdbcProvider.reloadCustomJdbcClassLoader(deDriver);
    }


    public DataDriverDetails saveJar(MultipartFile file, String driverId) throws Exception {
        DataDriver deDriver = deDriverMapper.selectByPrimaryKey(driverId);
        if(deDriver == null){
            throw new Exception(Translator.get("I18N_DRIVER_NOT_FOUND"));
        }
        String filename = file.getOriginalFilename();
        if(!filename.endsWith(".jar")){
            throw new Exception(Translator.get("I18N_NOT_JAR"));
        }
        String dirPath = DRIVER_PATH + driverId + "/";
        String filePath = dirPath + Md5Utils.md5(filename) + ".jar";

        saveFile(file, dirPath, filePath);
        List<String> jdbcList = new ArrayList<>();
        String version = "";

        DataDriverDetails deDriverDetails = new DataDriverDetails();
        deDriverDetails.setId(UUID.randomUUID().toString());
        deDriverDetails.setDeDriverId(driverId);
        deDriverDetails.setVersion(version);
        deDriverDetails.setFileName(filename);
        deDriverDetails.setDriverClass(String.join(",", jdbcList));
        deDriverDetails.setIsTransName(true);
        deDriverDetails.setTransName(Md5Utils.md5(filename) + ".jar");

        DataDriverDetailsExample deDriverDetailsExample = new DataDriverDetailsExample();
        deDriverDetailsExample.createCriteria().andDeDriverIdEqualTo(driverId).andFileNameEqualTo(filename);
        if(CollectionUtil.isNotEmpty(deDriverDetailsMapper.selectByExample(deDriverDetailsExample))){
            throw new Exception("A file with the same name already exists：" + filename);
        }

        deDriverDetailsMapper.insert(deDriverDetails);
        log.info("插入驱动: "+deDriverDetails);
        DefaultJdbcProvider defaultJdbcProvider = (DefaultJdbcProvider)ProviderFactory.getProvider(deDriver.getType());
        defaultJdbcProvider.reloadCustomJdbcClassLoader(deDriver);
        return deDriverDetails;
    }


    private String saveFile(MultipartFile file, String dirPath, String filePath) throws Exception {
        File p = new File(dirPath);
        if (!p.exists()) {
            p.mkdirs();
        }
        File f = new File(filePath);
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
        return filePath;
    }

}