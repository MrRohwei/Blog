package com.voidpen.server.module.tag.model.response;

import com.voidpen.server.module.tag.entity.TTag;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TagVO {

    private Long id;

    private String name;

    private String color;

    private Long blogCount;

    private LocalDateTime createdAt;

    public static TagVO from(TTag tag) {
        TagVO tagVO = new TagVO();
        tagVO.setId(tag.getId());
        tagVO.setName(tag.getName());
        tagVO.setColor(tag.getColor());
        tagVO.setCreatedAt(tag.getCreatedAt());
        tagVO.setBlogCount(0L);
        return tagVO;
    }
}
