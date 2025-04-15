package org.zfy.StuSelect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfy.StuSelect.entity.dto.AdminDashboardDTO;
import org.zfy.StuSelect.entity.dto.StudentDashboardDTO;
import org.zfy.StuSelect.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/student")
    public ResponseEntity<StudentDashboardDTO> getStudentDashboardData(@RequestParam Integer studentId) {
        StudentDashboardDTO data = dashboardService.getStudentDashboardData(studentId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboardData() {
        AdminDashboardDTO data = dashboardService.getAdminDashboardData();
        return ResponseEntity.ok(data);
    }
}