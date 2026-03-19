package com.voidpen.server.module.advertisement.controller;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.advertisement.model.request.AdvertisementQueryRequest;
import com.voidpen.server.module.advertisement.model.request.CreateAdvertisementRequest;
import com.voidpen.server.module.advertisement.model.request.UpdateAdvertisementRequest;
import com.voidpen.server.module.advertisement.model.request.UpdateAdvertisementStatusRequest;
import com.voidpen.server.module.advertisement.model.response.AdvertisementVO;
import com.voidpen.server.module.advertisement.service.AdvertisementService;
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

@Tag(name = "广告管理（后台）")
@RestController
@RequestMapping("/admin/v1/advertisements")
@RequiredArgsConstructor
public class AdminAdvertisementController {

    private final AdvertisementService advertisementService;

    @Operation(summary = "分页查询广告")
    @GetMapping
    public Result<PageResult<AdvertisementVO>> listAdvertisements(@Valid AdvertisementQueryRequest request) {
        return Result.success(advertisementService.listAdminAdvertisements(request));
    }

    @Operation(summary = "新建广告")
    @PostMapping
    public Result<Void> createAdvertisement(@RequestBody @Valid CreateAdvertisementRequest request) {
        advertisementService.createAdvertisement(request);
        return Result.success();
    }

    @Operation(summary = "编辑广告")
    @PutMapping("/{id}")
    public Result<Void> updateAdvertisement(
        @PathVariable Long id,
        @RequestBody @Valid UpdateAdvertisementRequest request
    ) {
        advertisementService.updateAdvertisement(id, request);
        return Result.success();
    }

    @Operation(summary = "删除广告")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAdvertisement(@PathVariable Long id) {
        advertisementService.deleteAdvertisement(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用广告")
    @PutMapping("/{id}/status")
    public Result<Void> updateAdvertisementStatus(
        @PathVariable Long id,
        @RequestBody @Valid UpdateAdvertisementStatusRequest request
    ) {
        advertisementService.updateAdvertisementStatus(id, request);
        return Result.success();
    }
}
