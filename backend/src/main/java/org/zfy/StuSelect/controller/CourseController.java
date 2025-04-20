package org.zfy.StuSelect.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zfy.StuSelect.entity.Course;
import org.zfy.StuSelect.security.JwtUserDetails;
import org.zfy.StuSelect.service.CourseService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    @GetMapping()
    public ResponseEntity<Page<Course>> getAvailableCourses(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            Authentication authentication) {
            
        // 从Authentication中获取用户ID
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getId();
        
        // 调用Service层获取课程列表和可选状态
        Page<Course> page = courseService.getAvailableCourses(current, size, keyword, userId);
        return ResponseEntity.ok(page);
    }
    
    @GetMapping("/admin")
    public ResponseEntity<Page<Course>> getCoursesForAdmin(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        
        // 调用Service层获取全部课程，不带可选状态处理
        Page<Course> page = courseService.pageCourse(current, size, keyword);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Integer id) {
        Course course = courseService.getById(id);
        return ResponseEntity.ok(course);
    }
    
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course createdCourse = courseService.createCourse(course);
        return ResponseEntity.ok(createdCourse);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Integer id, @RequestBody Course course) {
        course.setId(id);
        boolean updated = courseService.updateCourse(course);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCourseStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        boolean updated = courseService.updateCourseStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}