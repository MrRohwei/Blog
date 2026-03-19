package com.voidpen.server.module.tag.controller;

import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.tag.model.response.TagVO;
import com.voidpen.server.module.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "标签管理（前台）")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "获取标签列表（含博客数量）")
    @GetMapping
    public Result<List<TagVO>> listTags() {
        return Result.success(tagService.listPublicTags());
    }
}
