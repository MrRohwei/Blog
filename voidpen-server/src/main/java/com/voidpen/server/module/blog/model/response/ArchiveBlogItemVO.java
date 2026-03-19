package com.voidpen.server.module.blog.model.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ArchiveBlogItemVO {

    private Long id;

    private String title;

    private LocalDateTime createdAt;
}
