package com.bigdata.activity.serviceimpl;

import com.bigdata.activity.dao.ActCategoryDao;
import com.bigdata.activity.entity.ActCategory;
import com.bigdata.activity.service.ActCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 流程分类接口实现
 * @author Bigdata
 */
@Slf4j
@Service
@Transactional
public class ActCategoryServiceImpl implements ActCategoryService {

    @Autowired
    private ActCategoryDao actCategoryDao;

    @Override
    public ActCategoryDao getRepository() {
        return actCategoryDao;
    }

    @Override
    public List<ActCategory> findByParentIdOrderBySortOrder(String parentId) {

        return actCategoryDao.findByParentIdOrderBySortOrder(parentId);
    }

    @Override
    public List<ActCategory> findByTitleLikeOrderBySortOrder(String title) {

        return actCategoryDao.findByTitleLikeOrderBySortOrder(title);
    }
}