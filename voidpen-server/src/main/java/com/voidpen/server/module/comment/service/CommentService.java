package com.voidpen.server.module.comment.service;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.comment.model.request.CommentQueryRequest;
import com.voidpen.server.module.comment.model.request.CreateCommentRequest;
import com.voidpen.server.module.comment.model.request.UpdateCommentStatusRequest;
import com.voidpen.server.module.comment.model.response.AdminCommentVO;
import com.voidpen.server.module.comment.model.response.CommentVO;
import java.util.List;

public interface CommentService {

    List<CommentVO> listPublicComments(Long blogId);

    void createComment(Long userId, CreateCommentRequest request);

    PageResult<AdminCommentVO> listAdminComments(CommentQueryRequest request);

    void updateCommentStatus(Long id, UpdateCommentStatusRequest request);

    void deleteComment(Long id);
}
