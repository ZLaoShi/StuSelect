package org.zfy.StuSelect.entity.dto;

import lombok.Data;
import org.zfy.StuSelect.entity.Student;

@Data
public class RegisterRequest {
    // 用户基本信息
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String role;
    
    // 学生信息
    private Student student;
}