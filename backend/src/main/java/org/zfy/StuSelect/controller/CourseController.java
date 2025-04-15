package org.zfy.StuSelect.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zfy.StuSelect.entity.Course;
import org.zfy.StuSelect.service.CourseService;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    @GetMapping
    public ResponseEntity<Page<Course>> listCourses(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
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