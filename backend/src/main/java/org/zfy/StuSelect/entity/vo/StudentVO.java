package org.zfy.StuSelect.entity.vo;

import lombok.Data;
import org.zfy.StuSelect.entity.Student;
import org.zfy.StuSelect.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentVO extends Student {
    private User user;
}