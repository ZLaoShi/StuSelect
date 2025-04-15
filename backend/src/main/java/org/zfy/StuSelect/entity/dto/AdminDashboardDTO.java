package org.zfy.StuSelect.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private Integer totalStudentCount; // 学生总数
    private Integer totalCourseCount; // 课程总数
    private Integer activeCourseCount; // 活跃课程数
    private Integer totalSelectionCount; // 选课总数
    private Map<String, Integer> courseTypeDistribution; // 课程类型分布
    private List<Map<String, Object>> topSelectedCourses; // 选课人数最多的课程
    private List<Map<String, Object>> recentSelections; // 最近选课记录
}
