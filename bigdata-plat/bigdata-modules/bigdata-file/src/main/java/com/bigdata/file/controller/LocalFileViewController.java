package com.bigdata.file.controller;

import com.bigdata.core.common.exception.BigdataException;
import com.bigdata.file.entity.File;
import com.bigdata.file.manage.impl.LocalFileManage;
import com.bigdata.file.service.FileService;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;


/**
 * @author Bigdata
 */
@Slf4j
@Controller
@Api(tags = "文件管理管理接口")
@RequestMapping("/bigdata/file")
@Transactional
public class LocalFileViewController {

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "本地存储预览文件")
    public void view(@PathVariable String id,
                     @RequestParam(required = false) String filename,
                     @RequestParam(required = false, defaultValue = "false") Boolean preview,
                     @RequestParam(required = false, defaultValue = "UTF-8") String charset,
                     HttpServletResponse response) throws IOException {

        File file = fileService.get(id);
        if (file == null) {
            throw new BigdataException("文件ID：" + id + "不存在");
        }
        if (StrUtil.isBlank(filename)) {
            filename = file.getFKey();
        }

        if (!preview) {
            response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        }
        response.setContentLengthLong(file.getSize());
        response.setContentType(file.getType() + ";charset=" + charset);
        response.addHeader("Accept-Ranges", "bytes");
        if (file.getSize() != null && file.getSize() > 0) {
            response.addHeader("Content-Range", "bytes " + 0 + "-" + (file.getSize() - 1) + "/" + file.getSize());
        }
        response.setBufferSize(10 * 1024 * 1024);
        LocalFileManage.view(file.getUrl(), response);
    }
}
