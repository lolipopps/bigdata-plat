package com.bigdata.base.controller.manage;

import com.bigdata.core.common.constant.CommonConstant;
import com.bigdata.core.common.redis.RedisTemplateHelper;
import com.bigdata.core.common.utils.PageUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.vo.PageVo;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.dao.mapper.DeleteMapper;
import com.bigdata.core.entity.Role;
import com.bigdata.core.entity.RoleDepartment;
import com.bigdata.core.entity.RolePermission;
import com.bigdata.core.entity.UserRole;
import com.bigdata.core.service.RoleDepartmentService;
import com.bigdata.core.service.RolePermissionService;
import com.bigdata.core.service.RoleService;
import com.bigdata.core.service.UserRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Bigdata
 */
@Slf4j
@RestController
@Api(tags = "角色管理接口")
@RequestMapping("/bigdata/role")
@Transactional
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private RoleDepartmentService roleDepartmentService;

    @Autowired
    private DeleteMapper deleteMapper;

    @Autowired
    private RedisTemplateHelper redisTemplate;

    @RequestMapping(value = "/getAllList", method = RequestMethod.GET)
    @ApiOperation(value = "获取全部角色")
    public Result roleGetAll() {

        List<Role> list = roleService.getAll();
        return ResultUtil.data(list);
    }

    @RequestMapping(value = "/getAllByPage", method = RequestMethod.GET)
    @ApiOperation(value = "分页获取角色")
    public Result<Page<Role>> getRoleByPage(String key, PageVo page) {

        Page<Role> list = roleService.findByCondition(key, PageUtil.initPage(page));
        for (Role role : list.getContent()) {
            // 角色拥有权限
            List<RolePermission> permissions = rolePermissionService.findByRoleId(role.getId());
            role.setPermissions(permissions);
            // 角色拥有数据权限
            List<RoleDepartment> departments = roleDepartmentService.findByRoleId(role.getId());
            role.setDepartments(departments);
        }
        return new ResultUtil<Page<Role>>().setData(list);
    }

    @RequestMapping(value = "/setDefault", method = RequestMethod.POST)
    @ApiOperation(value = "设置或取消默认角色")
    public Result setDefault(@RequestParam String id,
                                     @RequestParam Boolean isDefault) {

        Role role = roleService.get(id);
        if (role == null) {
            return ResultUtil.error("角色不存在");
        }
        role.setDefaultRole(isDefault);
        roleService.update(role);
        return ResultUtil.success("设置成功");
    }

    @RequestMapping(value = "/editRolePerm", method = RequestMethod.POST)
    @ApiOperation(value = "编辑角色分配菜单权限")
    public Result editRolePerm(@RequestParam String roleId,
                                       @RequestParam(required = false) String[] permIds) {

        // 删除其关联权限
        rolePermissionService.deleteByRoleId(roleId);
        // 批量分配新权限
        if (permIds != null) {
            List<RolePermission> list = Arrays.asList(permIds).stream().map(e -> {
                return new RolePermission().setRoleId(roleId).setPermissionId(e);
            }).collect(Collectors.toList());
            rolePermissionService.saveOrUpdateAll(list);
        }
        // 手动批量删除缓存
        redisTemplate.deleteByPattern("user:*");
        redisTemplate.deleteByPattern("userRole:*");
        redisTemplate.deleteByPattern("permission::userMenuList:*");
        return ResultUtil.data(null);
    }

    @RequestMapping(value = "/editRoleDep", method = RequestMethod.POST)
    @ApiOperation(value = "编辑角色分配数据权限")
    public Result editRoleDep(@RequestParam String roleId,
                                      @RequestParam Integer dataType,
                                      @RequestParam(required = false) String[] depIds) {

        Role r = roleService.get(roleId);
        r.setDataType(dataType);
        roleService.update(r);
        if (CommonConstant.DATA_TYPE_CUSTOM.equals(dataType)) {
            // 删除其关联数据权限
            roleDepartmentService.deleteByRoleId(roleId);
            // 批量分配新数据权限
            if (depIds != null) {
                List<RoleDepartment> list = Arrays.asList(depIds).stream().map(e -> {
                    return new RoleDepartment().setRoleId(roleId).setDepartmentId(e);
                }).collect(Collectors.toList());
                roleDepartmentService.saveOrUpdateAll(list);
            }
        }
        // 手动删除相关缓存
        redisTemplate.deleteByPattern("department:*");
        redisTemplate.deleteByPattern("userRole:*");
        return ResultUtil.data(null);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ApiOperation(value = "保存数据")
    public Result<Role> save(Role role) {

        Role r = roleService.save(role);
        return new ResultUtil<Role>().setData(r);
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ApiOperation(value = "更新数据")
    public Result<Role> edit(Role entity) {

        Role r = roleService.update(entity);
        // 手动批量删除缓存
        redisTemplate.deleteByPattern("user:*");
        redisTemplate.deleteByPattern("userRole:*");
        return new ResultUtil<Role>().setData(r);
    }

    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    @ApiOperation(value = "批量通过ids删除")
    public Result delByIds(@RequestParam String[] ids) {

        for (String id : ids) {
            List<UserRole> list = userRoleService.findByRoleId(id);
            if (list != null && list.size() > 0) {
                return ResultUtil.error("删除失败，包含正被用户使用关联的角色");
            }
        }
        for (String id : ids) {
            roleService.delete(id);
            // 删除关联菜单权限
            rolePermissionService.deleteByRoleId(id);
            // 删除关联数据权限
            roleDepartmentService.deleteByRoleId(id);
            // 删除流程关联节点
            deleteMapper.deleteActNode(id);
        }
        return ResultUtil.success("批量通过id删除数据成功");
    }
}
