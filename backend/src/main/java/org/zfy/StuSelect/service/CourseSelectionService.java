package org.zfy.StuSelect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.zfy.StuSelect.entity.CourseSelection;

import java.util.List;

public interface CourseSelectionService extends IService<CourseSelection> {
    /**
     * 选课
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 选课记录
     */
    CourseSelection selectCourse(Integer studentId, Integer courseId);
    
    /**
     * 退课
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean dropCourse(Integer studentId, Integer courseId);
    
    /**
     * 获取学生的选课列表
     * @param studentId 学生ID
     * @return 选课列表
     */
    List<CourseSelection> getStudentCourses(Integer studentId);
    
    /**
     * 获取课程的学生列表
     * @param courseId 课程ID
     * @return 选课列表
     */
    List<CourseSelection> getCourseStudents(Integer courseId);
    
    /**
     * 设置成绩
     * @param selectionId 选课ID
     * @param score 成绩
     * @param scoreType 成绩类型
     * @return 是否成功
     */
    boolean setScore(Integer selectionId, Float score, String scoreType);

    List<CourseSelection> getStudentCoursesWithDetails(Integer studentId);
}