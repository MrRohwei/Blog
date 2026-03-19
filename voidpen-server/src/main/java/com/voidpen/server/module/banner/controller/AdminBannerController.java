package com.voidpen.server.module.banner.controller;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.banner.model.request.BannerQueryRequest;
import com.voidpen.server.module.banner.model.request.CreateBannerRequest;
import com.voidpen.server.module.banner.model.request.UpdateBannerRequest;
import com.voidpen.server.module.banner.model.request.UpdateBannerStatusRequest;
import com.voidpen.server.module.banner.model.response.BannerVO;
import com.voidpen.server.module.banner.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "轮播图管理（后台）")
@RestController
@RequestMapping("/admin/v1/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    @Operation(summary = "分页查询轮播图")
    @GetMapping
    public Result<PageResult<BannerVO>> listBanners(@Valid BannerQueryRequest request) {
        return Result.success(bannerService.listAdminBanners(request));
    }

    @Operation(summary = "新建轮播图")
    @PostMapping
    public Result<Void> createBanner(@RequestBody @Valid CreateBannerRequest request) {
        bannerService.createBanner(request);
        return Result.success();
    }

    @Operation(summary = "编辑轮播图")
    @PutMapping("/{id}")
    public Result<Void> updateBanner(@PathVariable Long id, @RequestBody @Valid UpdateBannerRequest request) {
        bannerService.updateBanner(id, request);
        return Result.success();
    }

    @Operation(summary = "删除轮播图")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用轮播图")
    @PutMapping("/{id}/status")
    public Result<Void> updateBannerStatus(
        @PathVariable Long id,
        @RequestBody @Valid UpdateBannerStatusRequest request
    ) {
        bannerService.updateBannerStatus(id, request);
        return Result.success();
    }
}
