package org.zfy.StuSelect.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.zfy.StuSelect.entity.User;

public interface UserService extends IService<User> {
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return JWT令牌
     */
    String login(String username, String password);
    
    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建的用户
     */
    User createUser(User user);
    
    /**
     * 修改用户状态
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateUserStatus(Integer userId, String status);

     /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
}