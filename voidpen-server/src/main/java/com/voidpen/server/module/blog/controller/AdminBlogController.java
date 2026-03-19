package com.voidpen.server.module.blog.controller;

import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.blog.model.request.BlogQueryRequest;
import com.voidpen.server.module.blog.model.request.BlogSaveRequest;
import com.voidpen.server.module.blog.model.request.UpdateBlogStatusRequest;
import com.voidpen.server.module.blog.model.request.UpdateBlogTopRequest;
import com.voidpen.server.module.blog.model.response.BlogListVO;
import com.voidpen.server.module.blog.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "博客管理（后台）")
@RestController
@RequestMapping("/admin/v1/blogs")
@RequiredArgsConstructor
public class AdminBlogController {

    private final BlogService blogService;

    @Operation(summary = "后台博客分页列表")
    @GetMapping
    public Result<PageResult<BlogListVO>> listBlogs(@Valid BlogQueryRequest request) {
        return Result.success(blogService.listAdminBlogs(request));
    }

    @Operation(summary = "新建博客")
    @PostMapping
    public Result<Void> createBlog(@RequestBody @Valid BlogSaveRequest request) {
        blogService.createBlog(currentUserId(), request);
        return Result.success();
    }

    @Operation(summary = "编辑博客")
    @PutMapping("/{id}")
    public Result<Void> updateBlog(@PathVariable Long id, @RequestBody @Valid BlogSaveRequest request) {
        blogService.updateBlog(id, request);
        return Result.success();
    }

    @Operation(summary = "删除博客")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return Result.success();
    }

    @Operation(summary = "修改博客状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid UpdateBlogStatusRequest request) {
        blogService.updateBlogStatus(id, request);
        return Result.success();
    }

    @Operation(summary = "置顶/取消置顶博客")
    @PutMapping("/{id}/top")
    public Result<Void> updateTop(@PathVariable Long id, @RequestBody @Valid UpdateBlogTopRequest request) {
        blogService.updateBlogTop(id, request);
        return Result.success();
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof String text && StringUtils.hasText(text)) {
            return Long.valueOf(text);
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
