package com.voidpen.server.module.blog.model.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BlogDetailVO {

    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    private String content;

    private String coverImg;

    private String summary;

    private Integer views;

    private Integer likes;

    private Integer status;

    private Integer isTop;

    private Integer isFeatured;

    private String categoryName;

    private String authorName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<BlogTagVO> tags = new ArrayList<>();

    public BlogDetailVO copy() {
        BlogDetailVO copy = new BlogDetailVO();
        copy.setId(this.id);
        copy.setUserId(this.userId);
        copy.setCategoryId(this.categoryId);
        copy.setTitle(this.title);
        copy.setContent(this.content);
        copy.setCoverImg(this.coverImg);
        copy.setSummary(this.summary);
        copy.setViews(this.views);
        copy.setLikes(this.likes);
        copy.setStatus(this.status);
        copy.setIsTop(this.isTop);
        copy.setIsFeatured(this.isFeatured);
        copy.setCategoryName(this.categoryName);
        copy.setAuthorName(this.authorName);
        copy.setCreatedAt(this.createdAt);
        copy.setUpdatedAt(this.updatedAt);
        copy.setTags(this.tags == null ? new ArrayList<>() : new ArrayList<>(this.tags));
        return copy;
    }
}
