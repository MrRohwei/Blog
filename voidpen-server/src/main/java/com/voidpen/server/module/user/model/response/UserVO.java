package com.voidpen.server.module.user.model.response;

import com.voidpen.server.module.user.entity.TUser;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String email;

    private String avatar;

    private String nickname;

    private String role;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static UserVO from(TUser user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setAvatar(user.getAvatar());
        userVO.setNickname(user.getNickname());
        userVO.setRole(user.getRole());
        userVO.setStatus(user.getStatus());
        userVO.setLastLoginTime(user.getLastLoginTime());
        userVO.setCreatedAt(user.getCreatedAt());
        userVO.setUpdatedAt(user.getUpdatedAt());
        return userVO;
    }
}
