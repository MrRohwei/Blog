package com.voidpen.server.module.category.model.response;

import com.voidpen.server.module.category.entity.TCategory;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CategoryVO {

    private Long id;

    private String name;

    private String description;

    private Integer sortOrder;

    private Integer status;

    private Long blogCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static CategoryVO from(TCategory category) {
        CategoryVO categoryVO = new CategoryVO();
        categoryVO.setId(category.getId());
        categoryVO.setName(category.getName());
        categoryVO.setDescription(category.getDescription());
        categoryVO.setSortOrder(category.getSortOrder());
        categoryVO.setStatus(category.getStatus());
        categoryVO.setCreatedAt(category.getCreatedAt());
        categoryVO.setUpdatedAt(category.getUpdatedAt());
        categoryVO.setBlogCount(0L);
        return categoryVO;
    }
}
