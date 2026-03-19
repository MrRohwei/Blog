package com.voidpen.server.module.banner.service;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.banner.model.request.BannerQueryRequest;
import com.voidpen.server.module.banner.model.request.CreateBannerRequest;
import com.voidpen.server.module.banner.model.request.UpdateBannerRequest;
import com.voidpen.server.module.banner.model.request.UpdateBannerStatusRequest;
import com.voidpen.server.module.banner.model.response.BannerVO;
import java.util.List;

public interface BannerService {

    List<BannerVO> listPublicBanners();

    PageResult<BannerVO> listAdminBanners(BannerQueryRequest request);

    void createBanner(CreateBannerRequest request);

    void updateBanner(Long id, UpdateBannerRequest request);

    void deleteBanner(Long id);

    void updateBannerStatus(Long id, UpdateBannerStatusRequest request);
}
