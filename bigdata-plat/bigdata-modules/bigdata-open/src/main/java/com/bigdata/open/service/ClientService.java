package com.bigdata.open.service;

import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.open.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 客户端接口
 * @author Bigdata
 */
public interface ClientService extends BigdataBaseService<Client, String> {

    /**
     * 多条件分页获取
     * @param client
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<Client> findByCondition(Client client, SearchVo searchVo, Pageable pageable);

}