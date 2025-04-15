package org.zfy.StuSelect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.zfy.StuSelect.entity.User;
import org.zfy.StuSelect.mapper.UserMapper;
import org.zfy.StuSelect.security.JwtUserDetails;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库加载用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
                
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 检查用户状态
        if (!"active".equals(user.getStatus())) {
            throw new UsernameNotFoundException("账号已被锁定或注销");
        }
        
        // 创建JwtUserDetails对象，包含用户ID信息
        return new JwtUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                "active".equals(user.getStatus())
        );
    }
}