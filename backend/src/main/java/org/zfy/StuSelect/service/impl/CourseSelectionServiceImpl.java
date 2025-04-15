package org.zfy.StuSelect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zfy.StuSelect.entity.Course;
import org.zfy.StuSelect.entity.CourseSelection;
import org.zfy.StuSelect.entity.Student;
import org.zfy.StuSelect.entity.User;
import org.zfy.StuSelect.mapper.CourseMapper;
import org.zfy.StuSelect.mapper.CourseSelectionMapper;
import org.zfy.StuSelect.mapper.StudentMapper;
import org.zfy.StuSelect.mapper.UserMapper;
import org.zfy.StuSelect.service.CourseSelectionService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseSelectionServiceImpl extends ServiceImpl<CourseSelectionMapper, CourseSelection> implements CourseSelectionService {

    private final CourseSelectionMapper courseSelectionMapper;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CourseSelection selectCourse(Integer studentId, Integer courseId) {
        // 检查学生是否存在
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }

        // 检查课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 检查课程状态
        if (!"active".equals(course.getStatus())) {
            throw new RuntimeException("该课程已取消或结束");
        }

        // 检查是否已经选过这门课
        if (courseSelectionMapper.selectCount(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getStatus, "selected")) > 0) {
            throw new RuntimeException("你已经选过这门课程");
        }

        // 检查是否有名额
        if (course.getSelectedCount() >= course.getCapacity()) {
            throw new RuntimeException("该课程已无剩余名额");
        }

        // 增加已选人数
        course.setSelectedCount(course.getSelectedCount() + 1);
        courseMapper.updateById(course);

        // 创建选课记录
        CourseSelection selection = new CourseSelection();
        selection.setStudentId(studentId);
        selection.setCourseId(courseId);
        selection.setSelectTime(LocalDateTime.now());
        selection.setStatus("selected");
        
        LocalDateTime now = LocalDateTime.now();
        selection.setCreatedAt(now);
        selection.setUpdatedAt(now);
        
        courseSelectionMapper.insert(selection);
        
        return selection;
    }

    @Override
    @Transactional
    public boolean dropCourse(Integer studentId, Integer courseId) {
        // 查找选课记录
        CourseSelection selection = courseSelectionMapper.selectOne(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getStatus, "selected"));
                
        if (selection == null) {
            throw new RuntimeException("未找到该选课记录");
        }
        
        // 更新选课状态
        selection.setStatus("dropped");
        selection.setUpdatedAt(LocalDateTime.now());
        courseSelectionMapper.updateById(selection);
        
        // 减少已选人数
        Course course = courseMapper.selectById(courseId);
        if (course != null && course.getSelectedCount() > 0) {
            course.setSelectedCount(course.getSelectedCount() - 1);
            courseMapper.updateById(course);
        }
        
        return true;
    }

    @Override
    public List<CourseSelection> getStudentCourses(Integer studentId) {
        return courseSelectionMapper.selectList(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .orderByDesc(CourseSelection::getCreatedAt));
    }

    @Override
    public List<CourseSelection> getCourseStudents(Integer courseId) {
        // 获取基础选课数据
        List<CourseSelection> selections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getCourseId, courseId)
                        .orderByAsc(CourseSelection::getCreatedAt));
        
        // 填充学生和用户详细信息
        for (CourseSelection selection : selections) {
            // 获取学生信息
            Student student = studentMapper.selectById(selection.getStudentId());
            if (student != null) {
                // 获取关联的用户信息
                User user = userMapper.selectById(student.getUserId());
                if (user != null) {
                    // 出于安全考虑，不返回密码
                    user.setPassword(null);
                    // 将用户信息设置到学生对象中
                    student.setUser(user);
                }
                selection.setStudent(student);
            }
            
            // 获取课程信息 (可选，如果需要课程详情)
            Course course = courseMapper.selectById(selection.getCourseId());
            if (course != null) {
                selection.setCourse(course);
            }
        }
        
        return selections;
    }

    @Override
    @Transactional
    public boolean setScore(Integer selectionId, Float score, String scoreType) {
        CourseSelection selection = courseSelectionMapper.selectById(selectionId);
        if (selection == null) {
            throw new RuntimeException("选课记录不存在");
        }
        
        // 更新成绩
        selection.setScore(score);
        selection.setScoreType(scoreType != null ? scoreType : "百分制");
        selection.setStatus("completed");
        selection.setUpdatedAt(LocalDateTime.now());
        
        return courseSelectionMapper.updateById(selection) > 0;
    }

    @Override
    public List<CourseSelection> getStudentCoursesWithDetails(Integer studentId) {
        // 1. 获取该学生的所有选课记录
        List<CourseSelection> selections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, studentId)
        );
        
        // 2. 为每个选课记录加载对应的课程信息
        for (CourseSelection selection : selections) {
            Course course = courseMapper.selectById(selection.getCourseId());
            selection.setCourse(course);
        }
        
        return selections;
    }
}