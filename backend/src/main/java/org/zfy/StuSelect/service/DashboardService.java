package org.zfy.StuSelect.service;

import org.zfy.StuSelect.entity.dto.AdminDashboardDTO;
import org.zfy.StuSelect.entity.dto.StudentDashboardDTO;

public interface DashboardService {
    /**
     * 获取学生仪表盘数据
     * @param studentId 学生ID
     * @return 学生仪表盘数据
     */
    StudentDashboardDTO getStudentDashboardData(Integer studentId);
    
    /**
     * 获取管理员仪表盘数据
     * @return 管理员仪表盘数据
     */
    AdminDashboardDTO getAdminDashboardData();
}