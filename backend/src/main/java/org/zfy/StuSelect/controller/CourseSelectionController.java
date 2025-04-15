package org.zfy.StuSelect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zfy.StuSelect.entity.CourseSelection;
import org.zfy.StuSelect.service.CourseSelectionService;

import java.util.List;

@RestController
@RequestMapping("/api/selections")
@RequiredArgsConstructor
public class CourseSelectionController {
    
    private final CourseSelectionService courseSelectionService;
    
    @PostMapping
    public ResponseEntity<CourseSelection> selectCourse(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId) {
        CourseSelection selection = courseSelectionService.selectCourse(studentId, courseId);
        return ResponseEntity.ok(selection);
    }
    
    @DeleteMapping
    public ResponseEntity<?> dropCourse(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId) {
        boolean dropped = courseSelectionService.dropCourse(studentId, courseId);
        return ResponseEntity.ok(dropped);
    }
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CourseSelection>> getStudentCourses(@PathVariable Integer studentId) {
        List<CourseSelection> selections = courseSelectionService.getStudentCourses(studentId);
        return ResponseEntity.ok(selections);
    }
    
    @GetMapping("/student/{studentId}/details")
    public ResponseEntity<List<CourseSelection>> getStudentCoursesWithDetails(@PathVariable Integer studentId) {
        List<CourseSelection> selections = courseSelectionService.getStudentCoursesWithDetails(studentId);
        return ResponseEntity.ok(selections);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseSelection>> getCourseStudents(@PathVariable Integer courseId) {
        List<CourseSelection> selections = courseSelectionService.getCourseStudents(courseId);
        return ResponseEntity.ok(selections);
    }
    
    @PutMapping("/{id}/score")
    public ResponseEntity<?> setScore(
            @PathVariable Integer id,
            @RequestParam Float score,
            @RequestParam(required = false) String scoreType) {
        boolean updated = courseSelectionService.setScore(id, score, scoreType);
        return ResponseEntity.ok(updated);
    }
}