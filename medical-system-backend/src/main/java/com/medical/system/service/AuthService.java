package com.medical.system.service;

import com.medical.system.dto.LoginDTO;
import com.medical.system.dto.RegisterDTO;
import com.medical.system.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO loginDTO);
    void register(RegisterDTO registerDTO);
}
