package org.zfy.StuSelect.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.zfy.StuSelect.entity.Student;
import org.zfy.StuSelect.entity.User;
import org.zfy.StuSelect.entity.vo.StudentVO;
import org.zfy.StuSelect.security.JwtUserDetails;
import org.zfy.StuSelect.service.StudentService;
import org.zfy.StuSelect.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<Page<StudentVO>> getStudents(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        Page<StudentVO> students = studentService.pageStudents(current, size, keyword);
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StudentVO> getStudentById(@PathVariable Integer id, Authentication authentication) {
        // 管理员可以访问任何学生信息
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // 学生只能访问自己的信息
            // 从JWT中获取当前用户ID
            Integer currentStudentId = null;
            if (authentication.getPrincipal() instanceof UserDetails) {
                // 这里需要根据您的JWT实现来获取学生ID
                // 假设从authentication中可以获取到studentId
                JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
                Student student = studentService.getStudentByUserId(userDetails.getId());
                if (student != null) {
                    currentStudentId = student.getId();
                }
            }
            
            // 如果不是本人的信息，返回403
            if (currentStudentId == null || !currentStudentId.equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        StudentVO student = studentService.getStudentVOById(id);
        return ResponseEntity.ok(student);
    }
    
    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student, @RequestParam Integer userId) {
        Student createdStudent = studentService.createStudent(student, userId);
        return ResponseEntity.ok(createdStudent);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Student> getStudentByUserId(@PathVariable Integer userId) {
        Student student = studentService.getStudentByUserId(userId);
        return ResponseEntity.ok(student);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateStudent(@PathVariable Integer id, @RequestBody Student student) {
        student.setId(id);
        boolean updated = studentService.updateById(student);
        return ResponseEntity.ok(updated);
    }
}
