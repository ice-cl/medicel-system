package com.medical.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.system.common.Result;
import com.medical.system.entity.SysUser;
import com.medical.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    public Result<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username) {
        return Result.success(userService.pageList(pageNum, pageSize, username));
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysUser user,
                               @RequestParam Long roleId) {
        userService.createUser(user, roleId);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user,
                               @RequestParam(required = false) Long roleId) {
        userService.updateUser(user, roleId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
