package com.voidpen.server.module.advertisement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.advertisement.entity.TAdvertisement;
import com.voidpen.server.module.advertisement.enums.AdvertisementPosition;
import com.voidpen.server.module.advertisement.mapper.AdvertisementMapper;
import com.voidpen.server.module.advertisement.model.request.AdvertisementQueryRequest;
import com.voidpen.server.module.advertisement.model.request.CreateAdvertisementRequest;
import com.voidpen.server.module.advertisement.model.request.UpdateAdvertisementRequest;
import com.voidpen.server.module.advertisement.model.request.UpdateAdvertisementStatusRequest;
import com.voidpen.server.module.advertisement.model.response.AdvertisementVO;
import com.voidpen.server.module.advertisement.service.AdvertisementService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementMapper advertisementMapper;

    @Override
    public List<AdvertisementVO> listPublicAdvertisements(String position) {
        validatePositionOrThrow(position, false);
        List<TAdvertisement> ads = advertisementMapper.selectList(
            new LambdaQueryWrapper<TAdvertisement>()
                .eq(TAdvertisement::getStatus, 1)
                .eq(StringUtils.hasText(position), TAdvertisement::getPosition, position)
                .and(
                    wrapper -> wrapper
                        .isNull(TAdvertisement::getExpiredAt)
                        .or()
                        .gt(TAdvertisement::getExpiredAt, LocalDateTime.now())
                )
                .orderByDesc(TAdvertisement::getCreatedAt)
        );
        return ads.stream().map(AdvertisementVO::from).toList();
    }

    @Override
    public PageResult<AdvertisementVO> listAdminAdvertisements(AdvertisementQueryRequest request) {
        validatePositionOrThrow(request.getPosition(), false);
        Page<TAdvertisement> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<TAdvertisement> wrapper = new LambdaQueryWrapper<TAdvertisement>()
            .eq(request.getStatus() != null, TAdvertisement::getStatus, request.getStatus())
            .eq(StringUtils.hasText(request.getPosition()), TAdvertisement::getPosition, request.getPosition())
            .like(StringUtils.hasText(request.getKeyword()), TAdvertisement::getTitle, request.getKeyword())
            .orderByDesc(TAdvertisement::getCreatedAt);
        IPage<TAdvertisement> pageData = advertisementMapper.selectPage(page, wrapper);
        return PageResult.of(pageData.convert(AdvertisementVO::from));
    }

    @Override
    public void createAdvertisement(CreateAdvertisementRequest request) {
        validatePositionOrThrow(request.getPosition(), true);
        TAdvertisement ad = new TAdvertisement()
            .setTitle(request.getTitle())
            .setImageUrl(request.getImageUrl())
            .setLinkUrl(request.getLinkUrl())
            .setPosition(request.getPosition())
            .setStatus(request.getStatus() == null ? 1 : request.getStatus())
            .setExpiredAt(request.getExpiredAt());
        advertisementMapper.insert(ad);
    }

    @Override
    public void updateAdvertisement(Long id, UpdateAdvertisementRequest request) {
        TAdvertisement existing = getRequiredAd(id);
        validatePositionOrThrow(request.getPosition(), true);
        TAdvertisement toUpdate = new TAdvertisement()
            .setId(id)
            .setTitle(request.getTitle())
            .setImageUrl(request.getImageUrl())
            .setLinkUrl(request.getLinkUrl())
            .setPosition(request.getPosition())
            .setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus())
            .setExpiredAt(request.getExpiredAt());
        advertisementMapper.updateById(toUpdate);
    }

    @Override
    public void deleteAdvertisement(Long id) {
        getRequiredAd(id);
        advertisementMapper.deleteById(id);
    }

    @Override
    public void updateAdvertisementStatus(Long id, UpdateAdvertisementStatusRequest request) {
        getRequiredAd(id);
        advertisementMapper.updateById(new TAdvertisement().setId(id).setStatus(request.getStatus()));
    }

    private TAdvertisement getRequiredAd(Long id) {
        TAdvertisement ad = advertisementMapper.selectById(id);
        if (ad == null) {
            throw new BusinessException(ErrorCode.ADVERTISEMENT_NOT_FOUND);
        }
        return ad;
    }

    private void validatePositionOrThrow(String position, boolean required) {
        if (!StringUtils.hasText(position)) {
            if (required) {
                throw new BusinessException(ErrorCode.ADVERTISEMENT_POSITION_INVALID);
            }
            return;
        }
        if (!AdvertisementPosition.isValid(position)) {
            throw new BusinessException(ErrorCode.ADVERTISEMENT_POSITION_INVALID);
        }
    }
}
