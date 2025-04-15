package org.zfy.StuSelect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_selection")
public class CourseSelection {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private Integer courseId;
    private LocalDateTime selectTime;
    private Float score;
    private String scoreType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 添加以下两个字段，使用@TableField注解标记为非数据库字段
    @TableField(exist = false)
    private Student student;
    
    @TableField(exist = false)
    private Course course;
}