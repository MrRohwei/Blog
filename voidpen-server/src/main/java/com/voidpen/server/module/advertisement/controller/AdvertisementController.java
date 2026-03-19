package com.voidpen.server.module.advertisement.controller;

import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.advertisement.model.response.AdvertisementVO;
import com.voidpen.server.module.advertisement.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "广告管理（前台）")
@RestController
@RequestMapping("/api/v1/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @Operation(summary = "获取有效广告")
    @GetMapping
    public Result<List<AdvertisementVO>> listAdvertisements(
        @RequestParam(required = false) String position
    ) {
        return Result.success(advertisementService.listPublicAdvertisements(position));
    }
}
