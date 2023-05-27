package com.bigdata.file.controller;

import com.bigdata.core.common.constant.OssConstant;
import com.bigdata.core.common.constant.SettingConstant;
import com.bigdata.core.common.exception.BigdataException;
import com.bigdata.core.common.redis.RedisTemplateHelper;
import com.bigdata.core.common.utils.PageUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.utils.SecurityUtil;
import com.bigdata.core.common.vo.PageVo;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.core.entity.User;
import com.bigdata.core.service.SettingService;
import com.bigdata.core.vo.OssSetting;
import com.bigdata.file.entity.File;
import com.bigdata.file.service.FileService;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Bigdata
 */
@Slf4j
@RestController
@Api(tags = "文件管理管理接口")
@RequestMapping("/bigdata/file")
@Transactional
public class FileController {

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Result<Page<File>> getFileList(File file, SearchVo searchVo, PageVo pageVo) {

        Page<File> page = fileService.getFileList(file, searchVo, pageVo, false);
        return new ResultUtil<Page<File>>().setData(page);
    }

    @RequestMapping(value = "/rename", method = RequestMethod.POST)
    @ApiOperation(value = "文件重命名")
    public Result renameUserFile(@RequestParam String id,
                                         @RequestParam String newKey,
                                         @RequestParam String newTitle) {

        fileService.rename(id, newKey, newTitle, false);
        return ResultUtil.data(null);
    }

    @RequestMapping(value = "/copy", method = RequestMethod.POST)
    @ApiOperation(value = "文件复制")
    public Result copy(@RequestParam String id) {

        fileService.copy(id, false);
        return ResultUtil.data(null);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ApiOperation(value = "文件删除")
    public Result delete(@RequestParam String[] ids) {

        fileService.delete(ids, false);
        return ResultUtil.data(null);
    }
}
