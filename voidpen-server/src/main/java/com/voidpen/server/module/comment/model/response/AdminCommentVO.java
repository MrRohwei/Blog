package com.voidpen.server.module.comment.model.response;

import com.voidpen.server.module.comment.entity.TComment;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminCommentVO {

    private Long id;

    private Long blogId;

    private Long userId;

    private Long parentId;

    private String nickname;

    private String email;

    private String avatar;

    private String content;

    private Integer status;

    private LocalDateTime createdAt;

    public static AdminCommentVO from(TComment comment) {
        AdminCommentVO vo = new AdminCommentVO();
        vo.setId(comment.getId());
        vo.setBlogId(comment.getBlogId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setNickname(comment.getNickname());
        vo.setEmail(comment.getEmail());
        vo.setAvatar(comment.getAvatar());
        vo.setContent(comment.getContent());
        vo.setStatus(comment.getStatus());
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }
}
