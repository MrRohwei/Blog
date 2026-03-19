package com.voidpen.server.module.banner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.banner.entity.TBanner;
import com.voidpen.server.module.banner.mapper.BannerMapper;
import com.voidpen.server.module.banner.model.request.BannerQueryRequest;
import com.voidpen.server.module.banner.model.request.CreateBannerRequest;
import com.voidpen.server.module.banner.model.request.UpdateBannerRequest;
import com.voidpen.server.module.banner.model.request.UpdateBannerStatusRequest;
import com.voidpen.server.module.banner.model.response.BannerVO;
import com.voidpen.server.module.banner.service.BannerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;

    @Override
    public List<BannerVO> listPublicBanners() {
        List<TBanner> banners = bannerMapper.selectList(
            new LambdaQueryWrapper<TBanner>()
                .eq(TBanner::getStatus, 1)
                .orderByAsc(TBanner::getSortOrder)
                .orderByDesc(TBanner::getCreatedAt)
        );
        return banners.stream().map(BannerVO::from).toList();
    }

    @Override
    public PageResult<BannerVO> listAdminBanners(BannerQueryRequest request) {
        Page<TBanner> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<TBanner> wrapper = new LambdaQueryWrapper<TBanner>()
            .like(StringUtils.hasText(request.getKeyword()), TBanner::getTitle, request.getKeyword())
            .orderByAsc(TBanner::getSortOrder)
            .orderByDesc(TBanner::getCreatedAt);
        IPage<TBanner> pageData = bannerMapper.selectPage(page, wrapper);
        return PageResult.of(pageData.convert(BannerVO::from));
    }

    @Override
    public void createBanner(CreateBannerRequest request) {
        TBanner banner = new TBanner()
            .setTitle(request.getTitle())
            .setImageUrl(request.getImageUrl())
            .setLinkUrl(request.getLinkUrl())
            .setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
            .setStatus(request.getStatus() == null ? 1 : request.getStatus());
        bannerMapper.insert(banner);
    }

    @Override
    public void updateBanner(Long id, UpdateBannerRequest request) {
        TBanner existing = getRequiredBanner(id);
        TBanner toUpdate = new TBanner()
            .setId(id)
            .setTitle(request.getTitle())
            .setImageUrl(request.getImageUrl())
            .setLinkUrl(request.getLinkUrl())
            .setSortOrder(request.getSortOrder() == null ? existing.getSortOrder() : request.getSortOrder())
            .setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
        bannerMapper.updateById(toUpdate);
    }

    @Override
    public void deleteBanner(Long id) {
        getRequiredBanner(id);
        bannerMapper.deleteById(id);
    }

    @Override
    public void updateBannerStatus(Long id, UpdateBannerStatusRequest request) {
        getRequiredBanner(id);
        bannerMapper.updateById(new TBanner().setId(id).setStatus(request.getStatus()));
    }

    private TBanner getRequiredBanner(Long id) {
        TBanner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException(ErrorCode.BANNER_NOT_FOUND);
        }
        return banner;
    }
}
