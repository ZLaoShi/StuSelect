package org.zfy.StuSelect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.zfy.StuSelect.entity.Student;
import org.zfy.StuSelect.entity.User;
import org.zfy.StuSelect.entity.dto.ApiResponse;
import org.zfy.StuSelect.entity.dto.RegisterRequest;
import org.zfy.StuSelect.service.StudentService;
import org.zfy.StuSelect.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final StudentService studentService;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        String token = userService.login(username, password);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // 检查用户名是否已存在
            if (userService.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "用户名已存在"));
            }
            
            // 创建用户实体
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRole(request.getRole());
            user.setStatus("active");
            
            // 保存用户
            boolean saved = userService.save(user);
            if (!saved) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "用户创建失败"));
            }
            
            // 如果是学生，创建并关联学生信息
            if ("student".equals(request.getRole()) && request.getStudent() != null) {
                Student student = request.getStudent();
                student.setUserId(user.getId()); // 使用已保存用户的ID
                studentService.save(student);
            }
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("userId", user.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "注册失败: " + e.getMessage()));
        }
    }
}