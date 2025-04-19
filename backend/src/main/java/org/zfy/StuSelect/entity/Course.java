package org.zfy.StuSelect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String courseCode;
    private String courseName;
    private Float credit;
    private String courseType;
    private String teacherName;
    private Integer capacity;
    private Integer selectedCount;
    private String semester;
    private String classroom;
    private String schedule;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

     @TableField(exist = false)
    private Boolean selectable; // 是否可选
}