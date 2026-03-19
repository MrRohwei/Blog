package com.voidpen.server.module.tag.controller;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.tag.model.request.CreateTagRequest;
import com.voidpen.server.module.tag.model.request.TagQueryRequest;
import com.voidpen.server.module.tag.model.request.UpdateTagRequest;
import com.voidpen.server.module.tag.model.response.TagVO;
import com.voidpen.server.module.tag.service.TagService;
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

@Tag(name = "标签管理（后台）")
@RestController
@RequestMapping("/admin/v1/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    @Operation(summary = "分页查询标签")
    @GetMapping
    public Result<PageResult<TagVO>> listAdminTags(@Valid TagQueryRequest request) {
        return Result.success(tagService.listAdminTags(request));
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<Void> createTag(@RequestBody @Valid CreateTagRequest request) {
        tagService.createTag(request);
        return Result.success();
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @RequestBody @Valid UpdateTagRequest request) {
        tagService.updateTag(id, request);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
