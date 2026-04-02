package com.medical.system.service;

import com.medical.system.vo.StatisticsVO;

public interface StatisticsService {
    StatisticsVO getStatistics(Long userId, String role);
}
