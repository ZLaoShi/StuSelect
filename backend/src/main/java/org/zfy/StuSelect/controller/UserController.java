package org.zfy.StuSelect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.zfy.StuSelect.entity.User;
import org.zfy.StuSelect.security.JwtUserDetails;
import org.zfy.StuSelect.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id, Authentication authentication) {
        // 检查当前用户是否有权限访问此用户ID的信息
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // 如果不是管理员，检查是否是查询自己的信息
        if (!isAdmin) {
            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
            if (userDetails.getId() != id) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        User user = userService.getById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 出于安全考虑，不返回密码
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateUser(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        // 确保不更新敏感字段
        user.setPassword(null);
        user.setUsername(null);
        
        boolean updated = userService.updateById(user);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Boolean> updateUserStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        boolean updated = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}