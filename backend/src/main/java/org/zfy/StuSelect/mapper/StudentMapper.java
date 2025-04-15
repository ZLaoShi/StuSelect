package org.zfy.StuSelect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.zfy.StuSelect.entity.Student;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}