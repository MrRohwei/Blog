package com.voidpen.server.module.comment.controller;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.comment.model.request.CommentQueryRequest;
import com.voidpen.server.module.comment.model.request.UpdateCommentStatusRequest;
import com.voidpen.server.module.comment.model.response.AdminCommentVO;
import com.voidpen.server.module.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论管理（后台）")
@RestController
@RequestMapping("/admin/v1/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @Operation(summary = "分页查询评论")
    @GetMapping
    public Result<PageResult<AdminCommentVO>> listComments(@Valid CommentQueryRequest request) {
        return Result.success(commentService.listAdminComments(request));
    }

    @Operation(summary = "审核评论状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid UpdateCommentStatusRequest request) {
        commentService.updateCommentStatus(id, request);
        return Result.success();
    }

    @Operation(summary = "删除评论（含子评论）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }
}
