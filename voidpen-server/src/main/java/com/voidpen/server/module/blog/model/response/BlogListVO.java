package com.voidpen.server.module.blog.model.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BlogListVO {

    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

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
}
