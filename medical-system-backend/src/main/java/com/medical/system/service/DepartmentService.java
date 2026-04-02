package com.medical.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.Department;
import java.util.List;

public interface DepartmentService extends IService<Department> {
    Page<Department> pageList(Integer pageNum, Integer pageSize, String deptName);
    List<Department> listAll();
    void createDept(Department department);
    void updateDept(Department department);
    void deleteDept(Long id);
}
