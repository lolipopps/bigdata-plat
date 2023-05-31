package com.bigdata.connect.service;

import com.bigdata.connect.dto.DataDriverDTO;
import com.bigdata.connect.entity.DataDriverDetails;
import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.connect.entity.DataDriver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 生成代码接口
 * @author huyt
 */
public interface DataDriverService extends BigdataBaseService<DataDriver, String> {


    /**
    * 多条件分页获取
    * @param dataDriver
    * @param searchVo
    * @param pageable
    * @return
    */
    Page<DataDriver> findByCondition(DataDriver dataDriver, SearchVo searchVo, Pageable pageable);

    public List<DataDriverDetails> listDriverDetails(String driverId);

    public List<DataDriverDTO> list() throws Exception;

    public void deleteDriverFile(String driverFileId) throws Exception;

    public DataDriverDetails saveJar(MultipartFile file, String driverId) throws Exception;

}