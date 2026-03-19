package com.voidpen.server.module.banner.model.response;

import com.voidpen.server.module.banner.entity.TBanner;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BannerVO {

    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static BannerVO from(TBanner banner) {
        BannerVO vo = new BannerVO();
        vo.setId(banner.getId());
        vo.setTitle(banner.getTitle());
        vo.setImageUrl(banner.getImageUrl());
        vo.setLinkUrl(banner.getLinkUrl());
        vo.setSortOrder(banner.getSortOrder());
        vo.setStatus(banner.getStatus());
        vo.setCreatedAt(banner.getCreatedAt());
        vo.setUpdatedAt(banner.getUpdatedAt());
        return vo;
    }
}
