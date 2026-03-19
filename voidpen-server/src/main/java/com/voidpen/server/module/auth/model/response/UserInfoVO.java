package com.voidpen.server.module.auth.model.response;

import com.voidpen.server.module.user.entity.TUser;
import lombok.Data;

@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String role;

    private Integer status;

    public static UserInfoVO from(TUser user) {
        UserInfoVO info = new UserInfoVO();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setNickname(user.getNickname());
        info.setAvatar(user.getAvatar());
        info.setRole(user.getRole());
        info.setStatus(user.getStatus());
        return info;
    }
}
