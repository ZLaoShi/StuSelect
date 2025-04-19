package org.zfy.StuSelect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zfy.StuSelect.entity.Course;
import org.zfy.StuSelect.entity.CourseSelection;
import org.zfy.StuSelect.entity.Student;
import org.zfy.StuSelect.entity.dto.AdminDashboardDTO;
import org.zfy.StuSelect.entity.dto.StudentDashboardDTO;
import org.zfy.StuSelect.mapper.CourseMapper;
import org.zfy.StuSelect.mapper.CourseSelectionMapper;
import org.zfy.StuSelect.mapper.StudentMapper;
import org.zfy.StuSelect.service.DashboardService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final CourseSelectionMapper courseSelectionMapper;

    @Override
    public StudentDashboardDTO getStudentDashboardData(Integer studentId) {
        StudentDashboardDTO dto = new StudentDashboardDTO();
        
        // 1. 获取所有活跃课程数量
        Long totalActiveCoursesLong = courseMapper.selectCount(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getStatus, "active")
        );
        Integer totalActiveCourses = totalActiveCoursesLong.intValue();
        
        // 2. 获取该学生所有状态的选课记录（包括已选、已完成、已退课）
        List<CourseSelection> allSelections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, studentId)
        );
        
        // 3. 统计不可选的课程数量 - 包含已选、已完成、已退课的课程
        Set<Integer> unavailableCourseIds = allSelections.stream()
                .map(CourseSelection::getCourseId)
                .collect(Collectors.toSet());
        
        // 4. 计算可选课程数量 = 总活跃课程数 - 不可选课程数
        dto.setAvailableCourseCount(totalActiveCourses - unavailableCourseIds.size());
        
        // 5. 获取学生已选课程数量 - 只统计状态为selected的课程
        Long selectedCountLong = courseSelectionMapper.selectCount(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, studentId)
                        .eq(CourseSelection::getStatus, "selected")
        );
        Integer selectedCount = selectedCountLong.intValue();
        dto.setSelectedCourseCount(selectedCount);
        
        // 3. 获取课程类型分布 - 仍然只统计已选课程的分布
        List<CourseSelection> selectedSelections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, studentId)
                        .eq(CourseSelection::getStatus, "selected")
        );
        
        Map<String, Integer> typeDistribution = new HashMap<>();
        for (CourseSelection selection : selectedSelections) {
            Course course = courseMapper.selectById(selection.getCourseId());
            if (course != null) {
                String type = course.getCourseType();
                typeDistribution.put(type, typeDistribution.getOrDefault(type, 0) + 1);
            }
        }
        dto.setCourseTypeDistribution(typeDistribution);
        
        // 4. 获取最近选择的课程（最多5个）
        List<Map<String, Object>> recentCourses = new ArrayList<>();
        List<CourseSelection> recentSelections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, studentId)
                        .eq(CourseSelection::getStatus, "selected")
                        .orderByDesc(CourseSelection::getSelectTime)
                        .last("LIMIT 5")
        );
        
        for (CourseSelection selection : recentSelections) {
            Course course = courseMapper.selectById(selection.getCourseId());
            if (course != null) {
                Map<String, Object> courseMap = new HashMap<>();
                courseMap.put("id", course.getId());
                courseMap.put("courseName", course.getCourseName());
                courseMap.put("courseCode", course.getCourseCode());
                courseMap.put("teacherName", course.getTeacherName());
                courseMap.put("credit", course.getCredit());
                courseMap.put("selectTime", selection.getSelectTime());
                recentCourses.add(courseMap);
            }
        }
        dto.setRecentSelectedCourses(recentCourses);
        
        // 5. 计算总学分 - 修改为只统计已完成课程的学分
        float totalCreditFloat = 0.0f;
        List<CourseSelection> completedSelections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, studentId)
                        .eq(CourseSelection::getStatus, "completed")
        );
        
        for (CourseSelection selection : completedSelections) {
            Course course = courseMapper.selectById(selection.getCourseId());
            if (course != null) {
                totalCreditFloat += course.getCredit(); // 使用 float 来累加学分
            }
        }
        dto.setTotalCredit((int) totalCreditFloat); // 最后转换为 Integer
        
        return dto;
    }

    @Override
    public AdminDashboardDTO getAdminDashboardData() {
        AdminDashboardDTO dto = new AdminDashboardDTO();
        
        // 1. 获取学生总数 - 修复 Long 到 Integer 的转换
        Long studentCountLong = studentMapper.selectCount(null);
        Integer studentCount = studentCountLong.intValue(); // 正确转换为 Integer
        dto.setTotalStudentCount(studentCount);
        
        // 2. 获取课程总数 - 修复 Long 到 Integer 的转换
        Long courseCountLong = courseMapper.selectCount(null);
        Integer courseCount = courseCountLong.intValue(); // 正确转换为 Integer
        dto.setTotalCourseCount(courseCount);
        
        // 3. 获取活跃课程数 - 修复 Long 到 Integer 的转换
        Long activeCourseCountLong = courseMapper.selectCount(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getStatus, "active")
        );
        Integer activeCourseCount = activeCourseCountLong.intValue(); // 正确转换为 Integer
        dto.setActiveCourseCount(activeCourseCount);
        
        // 4. 获取选课总数 - 修复 Long 到 Integer 的转换
        Long selectionCountLong = courseSelectionMapper.selectCount(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStatus, "selected")
        );
        Integer selectionCount = selectionCountLong.intValue(); // 正确转换为 Integer
        dto.setTotalSelectionCount(selectionCount);
        
        // 后续代码保持不变...
        // 5. 获取课程类型分布
        List<Course> allCourses = courseMapper.selectList(null);
        Map<String, Integer> typeDistribution = new HashMap<>();
        for (Course course : allCourses) {
            String type = course.getCourseType();
            typeDistribution.put(type, typeDistribution.getOrDefault(type, 0) + 1);
        }
        dto.setCourseTypeDistribution(typeDistribution);
        
        // 6. 获取选课人数最多的课程（Top 5）
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .orderByDesc(Course::getSelectedCount)
                        .last("LIMIT 5")
        );
        
        List<Map<String, Object>> topCourses = courses.stream().map(course -> {
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("id", course.getId());
            courseMap.put("courseName", course.getCourseName());
            courseMap.put("courseCode", course.getCourseCode());
            courseMap.put("teacherName", course.getTeacherName());
            courseMap.put("selectedCount", course.getSelectedCount());
            courseMap.put("capacity", course.getCapacity());
            return courseMap;
        }).collect(Collectors.toList());
        
        dto.setTopSelectedCourses(topCourses);
        
        // 7. 获取最近的选课记录（最多10条）
        List<CourseSelection> recentSelections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .orderByDesc(CourseSelection::getSelectTime)
                        .last("LIMIT 10")
        );
        
        List<Map<String, Object>> recentSelectionsData = new ArrayList<>();
        for (CourseSelection selection : recentSelections) {
            Map<String, Object> selectionMap = new HashMap<>();
            selectionMap.put("id", selection.getId());
            selectionMap.put("selectTime", selection.getSelectTime());
            
            Student student = studentMapper.selectById(selection.getStudentId());
            if (student != null) {
                selectionMap.put("studentNo", student.getStudentNo());
                selectionMap.put("studentName", student.getStudentNo()); // 实际应该用realName，这里暂用studentNo代替
            }
            
            Course course = courseMapper.selectById(selection.getCourseId());
            if (course != null) {
                selectionMap.put("courseName", course.getCourseName());
                selectionMap.put("courseCode", course.getCourseCode());
            }
            
            recentSelectionsData.add(selectionMap);
        }
        dto.setRecentSelections(recentSelectionsData);
        
        return dto;
    }
}