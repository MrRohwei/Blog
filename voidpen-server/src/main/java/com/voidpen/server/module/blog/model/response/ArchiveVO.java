package com.voidpen.server.module.blog.model.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ArchiveVO {

    private Integer year;

    private Integer month;

    private Long count;

    private List<ArchiveBlogItemVO> blogs = new ArrayList<>();
}
