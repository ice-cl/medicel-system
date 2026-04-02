package com.medical.system.controller;

import com.medical.system.common.Result;
import com.medical.system.service.StatisticsService;
import com.medical.system.vo.StatisticsVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public Result<StatisticsVO> getStatistics(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        String role = getCurrentRole(authentication);
        return Result.success(statisticsService.getStatistics(userId, role));
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            throw new RuntimeException("未获取到当前登录用户");
        }
        return (Long) authentication.getPrincipal();
    }

    private String getCurrentRole(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("未获取到当前登录用户角色");
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未获取到当前登录用户角色"));
    }
}
