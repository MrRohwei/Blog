package com.voidpen.server.module.comment.controller;

import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.comment.model.request.CreateCommentRequest;
import com.voidpen.server.module.comment.model.response.CommentVO;
import com.voidpen.server.module.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论管理（前台）")
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "获取博客评论树")
    @GetMapping("/{blogId}")
    public Result<List<CommentVO>> listComments(@PathVariable Long blogId) {
        return Result.success(commentService.listPublicComments(blogId));
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<Void> createComment(@RequestBody @Valid CreateCommentRequest request) {
        commentService.createComment(currentUserIdOrNull(), request);
        return Result.success();
    }

    private Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof String text && StringUtils.hasText(text) && !"anonymousUser".equals(text)) {
            try {
                return Long.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
