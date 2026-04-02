package com.medical.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    Page<SysUser> pageList(Integer pageNum, Integer pageSize, String username);
    void createUser(SysUser user, Long roleId);
    void updateUser(SysUser user, Long roleId);
    void deleteUser(Long id);
}
