package org.zfy.StuSelect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("student")
public class Student {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String studentNo;
    private String gender;
    private LocalDate birthDate;
    private Integer grade;
    private String major;
    private String className;
    private LocalDate admissionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 添加非数据库字段 - 关联的用户信息
    @TableField(exist = false)
    private User user;
}