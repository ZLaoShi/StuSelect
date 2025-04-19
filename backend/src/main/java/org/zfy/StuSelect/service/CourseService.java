package org.zfy.StuSelect.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zfy.StuSelect.entity.Course;

import java.util.List;

public interface CourseService extends IService<Course> {
     /**
     * 获取学生可选的课程列表，自动标记哪些课程已选
     * @param current 当前页
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param userId 当前用户ID
     * @return 课程分页结果，每个课程带有selectable标记
     */
    Page<Course> getAvailableCourses(long current, long size, String keyword, Integer userId);
    
    /**
     * 分页查询课程
     * @param current 当前页
     * @param size 每页大小
     * @param keyword 关键词
     * @return 分页结果
     */
    Page<Course> pageCourse(long current, long size, String keyword);
    
    /**
     * 创建课程
     * @param course 课程信息
     * @return 创建的课程
     */
    Course createCourse(Course course);
    
    /**
     * 更新课程
     * @param course 课程信息
     * @return 是否成功
     */
    boolean updateCourse(Course course);
    
    /**
     * 修改课程状态
     * @param courseId 课程ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateCourseStatus(Integer courseId, String status);
}