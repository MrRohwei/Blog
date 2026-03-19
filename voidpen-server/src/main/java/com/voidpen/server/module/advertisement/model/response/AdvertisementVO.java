package com.voidpen.server.module.advertisement.model.response;

import com.voidpen.server.module.advertisement.entity.TAdvertisement;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdvertisementVO {

    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    private String position;

    private Integer status;

    private LocalDateTime expiredAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static AdvertisementVO from(TAdvertisement advertisement) {
        AdvertisementVO vo = new AdvertisementVO();
        vo.setId(advertisement.getId());
        vo.setTitle(advertisement.getTitle());
        vo.setImageUrl(advertisement.getImageUrl());
        vo.setLinkUrl(advertisement.getLinkUrl());
        vo.setPosition(advertisement.getPosition());
        vo.setStatus(advertisement.getStatus());
        vo.setExpiredAt(advertisement.getExpiredAt());
        vo.setCreatedAt(advertisement.getCreatedAt());
        vo.setUpdatedAt(advertisement.getUpdatedAt());
        return vo;
    }
}
