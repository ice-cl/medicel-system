package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.Department;
import com.medical.system.mapper.DepartmentMapper;
import com.medical.system.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public Page<Department> pageList(Integer pageNum, Integer pageSize, String deptName) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getDeleted, 0);
        if (StringUtils.hasText(deptName)) {
            wrapper.like(Department::getDeptName, deptName);
        }
        wrapper.orderByAsc(Department::getSortOrder);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<Department> listAll() {
        return list(new LambdaQueryWrapper<Department>()
                .eq(Department::getDeleted, 0)
                .eq(Department::getStatus, 1)
                .orderByAsc(Department::getSortOrder));
    }

    @Override
    public void createDept(Department department) {
        department.setDeleted(0);
        department.setStatus(1);
        save(department);
    }

    @Override
    public void updateDept(Department department) {
        updateById(department);
    }

    @Override
    public void deleteDept(Long id) {
        Department dept = new Department();
        dept.setId(id);
        dept.setDeleted(1);
        updateById(dept);
    }
}
