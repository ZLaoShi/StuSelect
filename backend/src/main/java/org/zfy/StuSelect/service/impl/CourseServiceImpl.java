package org.zfy.StuSelect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.zfy.StuSelect.entity.Course;
import org.zfy.StuSelect.entity.CourseSelection;
import org.zfy.StuSelect.entity.Student;
import org.zfy.StuSelect.mapper.CourseMapper;
import org.zfy.StuSelect.mapper.CourseSelectionMapper;
import org.zfy.StuSelect.mapper.StudentMapper;
import org.zfy.StuSelect.service.CourseService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;
    private final CourseSelectionMapper courseSelectionMapper;

    @Override
    public Page<Course> getAvailableCourses(long current, long size, String keyword, Integer userId) {
        // 1. 获取学生信息
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId));
        
        if (student == null) {
            throw new RuntimeException("学生信息不存在");
        }
        
        // 2. 获取该学生所有状态的课程ID列表，包括已选和已完成的
        List<CourseSelection> selections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, student.getId()));
                        
        // 创建一个Map，key是courseId，value是selection状态
        Map<Integer, String> courseSelectionStatusMap = selections.stream()
                .collect(Collectors.toMap(
                    CourseSelection::getCourseId,
                    CourseSelection::getStatus,
                    // 如果有冲突（同一课程多条记录），保留最后一条
                    (existing, replacement) -> replacement
                ));
        
        // 3. 分页查询课程
        Page<Course> page = pageCourse(current, size, keyword);
        
        // 4. 为每个课程标记是否可选
        for (Course course : page.getRecords()) {
            // 获取该课程的选课状态，如果没有选过则为null
            String selectionStatus = courseSelectionStatusMap.get(course.getId());
            
            // 如果学生已选且状态为selected，则不可选
            if ("selected".equals(selectionStatus)) {
                course.setSelectable(false);
            }
            // 如果学生已完成，则不可选
            else if ("completed".equals(selectionStatus)) {
                course.setSelectable(false);
            }
            // 如果学生已退课，也不可选
            else if ("dropped".equals(selectionStatus)) {
                course.setSelectable(false);
            }
            // 课程未开放 -> 不可选
            else if (!"active".equals(course.getStatus())) {
                course.setSelectable(false);
            }
            // 课程已满 -> 不可选
            else if (course.getSelectedCount() >= course.getCapacity()) {
                course.setSelectable(false);
            }
            // 其他情况 -> 可选
            else {
                course.setSelectable(true);
            }
        }
        
        return page;
    }

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