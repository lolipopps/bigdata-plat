package com.bigdata.base.utils;

import com.bigdata.base.vo.MenuVo;
import com.bigdata.core.entity.Permission;
import cn.hutool.core.bean.BeanUtil;

/**
 * @author Bigdata
 */
public class VoUtil {

    public static MenuVo permissionToMenuVo(Permission p) {

        MenuVo menuVo = new MenuVo();
        BeanUtil.copyProperties(p, menuVo);
        return menuVo;
    }
}
