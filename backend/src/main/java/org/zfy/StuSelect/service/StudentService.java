package org.zfy.StuSelect.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zfy.StuSelect.entity.Student;
import org.zfy.StuSelect.entity.vo.StudentVO;

public interface StudentService extends IService<Student> {
    /**
     * 创建学生
     * @param student 学生信息
     * @param userId 用户ID
     * @return 创建的学生
     */
    Student createStudent(Student student, Integer userId);
    
    /**
     * 根据用户ID获取学生信息
     * @param userId 用户ID
     * @return 学生信息
     */
    Student getStudentByUserId(Integer userId);
    
    /**
     * 分页查询学生
     * @param current 当前页
     * @param size 每页大小
     * @param keyword 关键词
     * @return 分页结果
     */
    Page<StudentVO> pageStudents(long current, long size, String keyword);
    
    /**
     * 根据ID获取学生VO信息（包含用户信息）
     * @param id 学生ID
     * @return 学生VO
     */
    StudentVO getStudentVOById(Integer id);
}