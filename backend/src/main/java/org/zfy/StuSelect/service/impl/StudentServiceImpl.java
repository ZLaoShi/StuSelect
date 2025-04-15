package org.zfy.StuSelect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.zfy.StuSelect.entity.Student;
import org.zfy.StuSelect.entity.User;
import org.zfy.StuSelect.mapper.StudentMapper;
import org.zfy.StuSelect.mapper.UserMapper;
import org.zfy.StuSelect.service.StudentService;
import org.zfy.StuSelect.entity.vo.StudentVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private final StudentMapper studentMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Student createStudent(Student student, Integer userId) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查学号是否已存在
        if (studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, student.getStudentNo())) > 0) {
            throw new RuntimeException("学号已存在");
        }
        
        // 检查用户是否已关联学生
        if (studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)) > 0) {
            throw new RuntimeException("该用户已关联学生信息");
        }
        
        // 设置用户ID和时间
        student.setUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        student.setCreatedAt(now);
        student.setUpdatedAt(now);
        
        studentMapper.insert(student);
        return student;
    }

    @Override
    public Student getStudentByUserId(Integer userId) {
        return studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId));
    }
    
    @Override
    public Page<StudentVO> pageStudents(long current, long size, String keyword) {
        Page<Student> page = new Page<>(current, size);
        
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果有关键词，则添加模糊查询条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Student::getStudentNo, keyword)
                    .or()
                    .like(Student::getClassName, keyword);
        }
        
        // 默认按照创建时间倒序排序
        queryWrapper.orderByDesc(Student::getCreatedAt);
        
        Page<Student> studentPage = studentMapper.selectPage(page, queryWrapper);
        
        // 转换为StudentVO
        Page<StudentVO> voPage = new Page<>(studentPage.getCurrent(), studentPage.getSize(), studentPage.getTotal());
        List<StudentVO> voList = new ArrayList<>();
        
        for (Student student : studentPage.getRecords()) {
            StudentVO vo = convertToVO(student);
            voList.add(vo);
        }
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public StudentVO getStudentVOById(Integer id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            return null;
        }
        
        return convertToVO(student);
    }
    
    private StudentVO convertToVO(Student student) {
        StudentVO vo = new StudentVO();
        // 复制Student的所有属性
        vo.setId(student.getId());
        vo.setUserId(student.getUserId());
        vo.setStudentNo(student.getStudentNo());
        vo.setGender(student.getGender());
        vo.setBirthDate(student.getBirthDate());
        vo.setGrade(student.getGrade());
        vo.setMajor(student.getMajor());
        vo.setClassName(student.getClassName());
        vo.setAdmissionDate(student.getAdmissionDate());
        vo.setCreatedAt(student.getCreatedAt());
        vo.setUpdatedAt(student.getUpdatedAt());
        
        // 获取关联的用户信息
        User user = userMapper.selectById(student.getUserId());
        if (user != null) {
            // 出于安全考虑，不返回密码
            user.setPassword(null);
            vo.setUser(user);
        }
        
        return vo;
    }
}