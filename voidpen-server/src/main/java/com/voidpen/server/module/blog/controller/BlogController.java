package com.voidpen.server.module.blog.controller;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.blog.model.request.BlogQueryRequest;
import com.voidpen.server.module.blog.model.response.ArchiveVO;
import com.voidpen.server.module.blog.model.response.BlogDetailVO;
import com.voidpen.server.module.blog.model.response.BlogListVO;
import com.voidpen.server.module.blog.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "博客管理（前台）")
@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @Operation(summary = "前台博客分页列表")
    @GetMapping
    public Result<PageResult<BlogListVO>> listBlogs(@Valid BlogQueryRequest request) {
        return Result.success(blogService.listPublicBlogs(request));
    }

    @Operation(summary = "获取博客详情")
    @GetMapping("/{id}")
    public Result<BlogDetailVO> getBlogDetail(@PathVariable Long id) {
        return Result.success(blogService.getPublicBlogDetail(id));
    }

    @Operation(summary = "获取置顶推荐博客")
    @GetMapping("/featured")
    public Result<List<BlogListVO>> featuredBlogs() {
        return Result.success(blogService.listFeaturedBlogs());
    }

    @Operation(summary = "获取归档时间轴")
    @GetMapping("/archive")
    public Result<List<ArchiveVO>> archiveBlogs() {
        return Result.success(blogService.listArchiveBlogs());
    }

    @Operation(summary = "博客点赞")
    @PostMapping("/{id}/like")
    public Result<Void> likeBlog(@PathVariable Long id, HttpServletRequest request) {
        blogService.likeBlog(id, extractClientIp(request));
        return Result.success();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
