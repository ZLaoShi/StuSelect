package org.zfy.StuSelect;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTest {

    @Test
    public void encodePassword() {
        // 创建密码编码器
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 要加密的密码
        String rawPassword = "admin123";
        
        // 加密密码
        String encodedPassword = encoder.encode(rawPassword);
        
        // 打印加密后的密码
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        
        // 验证密码是否匹配
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("密码匹配: " + matches);
        
    }
}