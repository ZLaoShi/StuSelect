package org.zfy.StuSelect.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardDTO {
    private Integer selectedCourseCount; // 已选课程数量
    private Integer availableCourseCount; // 可选课程数量
    private Map<String, Integer> courseTypeDistribution; // 课程类型分布
    private List<Map<String, Object>> recentSelectedCourses; // 最近选择的课程
    private Integer totalCredit; // 总学分
}