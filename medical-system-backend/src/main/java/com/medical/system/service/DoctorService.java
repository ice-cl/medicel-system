package com.medical.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.Doctor;
import com.medical.system.vo.DoctorVO;
import java.util.List;

public interface DoctorService extends IService<Doctor> {
    Page<Doctor> pageList(Integer pageNum, Integer pageSize, Long deptId);
    Page<Doctor> pagePendingList(Integer pageNum, Integer pageSize);
    List<Doctor> listByDept(Long deptId);
    List<DoctorVO> listDoctorVO(Long deptId);
    Doctor getByUserId(Long userId);
    void createDoctor(Doctor doctor);
    void updateDoctor(Doctor doctor);
    void deleteDoctor(Long id);
    void verifyDoctor(Long id, Integer status);
}
