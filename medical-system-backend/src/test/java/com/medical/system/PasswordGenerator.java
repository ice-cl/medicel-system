package com.medical.system;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Password: " + rawPassword);
        System.out.println("Encoded: " + encodedPassword);

        // 验证
        String testHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        System.out.println("Test hash matches 123456: " + encoder.matches("123456", testHash));
        System.out.println("New hash matches 123456: " + encoder.matches("123456", encodedPassword));
    }
}
