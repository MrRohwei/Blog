package com.voidpen.server.module.banner.controller;

import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.banner.model.response.BannerVO;
import com.voidpen.server.module.banner.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "轮播图管理（前台）")
@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @Operation(summary = "获取启用轮播图")
    @GetMapping
    public Result<List<BannerVO>> listBanners() {
        return Result.success(bannerService.listPublicBanners());
    }
}
