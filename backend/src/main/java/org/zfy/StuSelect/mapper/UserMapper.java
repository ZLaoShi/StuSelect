package org.zfy.StuSelect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.zfy.StuSelect.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}