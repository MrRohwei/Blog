package com.voidpen.server.module.blog.model.response;

import lombok.Data;

@Data
public class BlogTagRelationVO {

    private Long blogId;

    private Long tagId;

    private String tagName;

    private String tagColor;
}
