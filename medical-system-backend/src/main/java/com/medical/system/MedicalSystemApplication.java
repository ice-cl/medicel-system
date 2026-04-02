package com.medical.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.medical.system.mapper")
public class MedicalSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalSystemApplication.class, args);
    }
}
