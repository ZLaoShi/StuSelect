package org.zfy.StuSelect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.zfy.StuSelect.entity.Course;
import org.zfy.StuSelect.mapper.CourseMapper;
import org.zfy.StuSelect.service.CourseService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    public Page<Course> pageCourse(long current, long size, String keyword) {
        Page<Course> page = new Page<>(current, size);
        
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果有关键词，则添加模糊查询条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Course::getCourseName, keyword)
                    .or()
                    .like(Course::getCourseCode, keyword)
                    .or()
                    .like(Course::getTeacherName, keyword);
        }
        
        // 默认按照创建时间倒序排序
        queryWrapper.orderByDesc(Course::getCreatedAt);
        
        return courseMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional
    public Course createCourse(Course course) {
        // 检查课程代码是否已存在
        if (courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseCode, course.getCourseCode())) > 0) {
            throw new RuntimeException("课程代码已存在");
        }
        
        // 设置默认值
        if (course.getSelectedCount() == null) {
            course.setSelectedCount(0);
        }
        if (course.getStatus() == null) {
            course.setStatus("active");
        }
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        course.setCreatedAt(now);
        course.setUpdatedAt(now);
        
        courseMapper.insert(course);
        return course;
    }

    @Override
    @Transactional
    public boolean updateCourse(Course course) {
        Course existingCourse = courseMapper.selectById(course.getId());
        if (existingCourse == null) {
            throw new RuntimeException("课程不存在");
        }
        
        // 如果修改了课程代码，需要检查是否已存在
        if (!existingCourse.getCourseCode().equals(course.getCourseCode()) &&
                courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                        .eq(Course::getCourseCode, course.getCourseCode())) > 0) {
            throw new RuntimeException("课程代码已存在");
        }
        
        // 更新时间
        course.setUpdatedAt(LocalDateTime.now());
        
        return courseMapper.updateById(course) > 0;
    }

    @Override
    @Transactional
    public boolean updateCourseStatus(Integer courseId, String status) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        course.setStatus(status);
        course.setUpdatedAt(LocalDateTime.now());
        
        return courseMapper.updateById(course) > 0;
    }
}