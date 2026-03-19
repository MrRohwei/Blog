package com.voidpen.server.module.comment.model.response;

import com.voidpen.server.module.comment.entity.TComment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CommentVO {

    private Long id;

    private Long blogId;

    private Long parentId;

    private String nickname;

    private String avatar;

    private String content;

    private LocalDateTime createdAt;

    private List<CommentVO> children = new ArrayList<>();

    public static CommentVO from(TComment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setBlogId(comment.getBlogId());
        vo.setParentId(comment.getParentId());
        vo.setNickname(comment.getNickname());
        vo.setAvatar(comment.getAvatar());
        vo.setContent(comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }
}
