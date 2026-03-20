package com.voidpen.server.module.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.http.HtmlUtil;
import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.blog.entity.TBlog;
import com.voidpen.server.module.blog.mapper.BlogMapper;
import com.voidpen.server.module.comment.entity.TComment;
import com.voidpen.server.module.comment.mapper.CommentMapper;
import com.voidpen.server.module.comment.model.request.CommentQueryRequest;
import com.voidpen.server.module.comment.model.request.CreateCommentRequest;
import com.voidpen.server.module.comment.model.request.UpdateCommentStatusRequest;
import com.voidpen.server.module.comment.model.response.AdminCommentVO;
import com.voidpen.server.module.comment.model.response.CommentVO;
import com.voidpen.server.module.comment.service.CommentService;
import com.voidpen.server.module.user.entity.TUser;
import com.voidpen.server.module.user.mapper.UserMapper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Integer STATUS_PENDING = 0;

    private static final Integer STATUS_APPROVED = 1;

    private final CommentMapper commentMapper;

    private final BlogMapper blogMapper;

    private final UserMapper userMapper;

    @Override
    public List<CommentVO> listPublicComments(Long blogId) {
        validatePublishedBlog(blogId);
        List<TComment> comments = commentMapper.selectList(
            new LambdaQueryWrapper<TComment>()
                .eq(TComment::getBlogId, blogId)
                .eq(TComment::getStatus, STATUS_APPROVED)
                .orderByAsc(TComment::getCreatedAt)
        );
        if (comments.isEmpty()) {
            return List.of();
        }

        Map<Long, CommentVO> voMap = new HashMap<>(comments.size());
        comments.forEach(comment -> voMap.put(comment.getId(), CommentVO.from(comment)));

        List<CommentVO> roots = new ArrayList<>();
        for (TComment comment : comments) {
            CommentVO vo = voMap.get(comment.getId());
            Long parentId = comment.getParentId();
            if (parentId == null) {
                roots.add(vo);
                continue;
            }

            CommentVO parent = voMap.get(parentId);
            if (parent != null) {
                parent.getChildren().add(vo);
            } else {
                roots.add(vo);
            }
        }
        return roots;
    }

    @Override
    public void createComment(Long userId, CreateCommentRequest request) {
        validatePublishedBlog(request.getBlogId());
        validateParentComment(request.getBlogId(), request.getParentId());

        TComment comment = new TComment()
            .setBlogId(request.getBlogId())
            .setParentId(request.getParentId())
            .setContent(HtmlUtil.cleanHtmlTag(request.getContent()))
            .setStatus(STATUS_PENDING);

        if (userId != null) {
            TUser user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
            comment.setUserId(user.getId());
            comment.setNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
            comment.setEmail(user.getEmail());
            comment.setAvatar(user.getAvatar());
        } else {
            if (!StringUtils.hasText(request.getNickname()) || !StringUtils.hasText(request.getEmail())) {
                throw new BusinessException(ErrorCode.COMMENT_GUEST_INFO_REQUIRED);
            }
            comment.setNickname(request.getNickname());
            comment.setEmail(request.getEmail());
        }

        commentMapper.insert(comment);
    }

    @Override
    public PageResult<AdminCommentVO> listAdminComments(CommentQueryRequest request) {
        Page<TComment> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<TComment> wrapper = new LambdaQueryWrapper<TComment>()
            .eq(request.getStatus() != null, TComment::getStatus, request.getStatus())
            .eq(request.getBlogId() != null, TComment::getBlogId, request.getBlogId())
            .like(StringUtils.hasText(request.getKeyword()), TComment::getContent, request.getKeyword())
            .orderByDesc(TComment::getCreatedAt);
        IPage<TComment> pageData = commentMapper.selectPage(page, wrapper);
        return PageResult.of(pageData.convert(AdminCommentVO::from));
    }

    @Override
    public void updateCommentStatus(Long id, UpdateCommentStatusRequest request) {
        getRequiredComment(id);
        commentMapper.updateById(new TComment().setId(id).setStatus(request.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        TComment root = getRequiredComment(id);
        List<TComment> allComments = commentMapper.selectList(
            new LambdaQueryWrapper<TComment>()
                .eq(TComment::getBlogId, root.getBlogId())
                .select(TComment::getId, TComment::getParentId)
        );

        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (TComment comment : allComments) {
            if (comment.getParentId() == null) {
                continue;
            }
            childrenMap.computeIfAbsent(comment.getParentId(), key -> new ArrayList<>()).add(comment.getId());
        }

        Set<Long> toDelete = new HashSet<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        queue.add(id);
        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            if (!toDelete.add(currentId)) {
                continue;
            }
            List<Long> children = childrenMap.get(currentId);
            if (children != null) {
                queue.addAll(children);
            }
        }
        commentMapper.deleteBatchIds(toDelete);
    }

    private void validatePublishedBlog(Long blogId) {
        TBlog blog = blogMapper.selectById(blogId);
        if (blog == null || !Objects.equals(blog.getStatus(), STATUS_APPROVED)) {
            throw new BusinessException(ErrorCode.BLOG_NOT_FOUND);
        }
    }

    private void validateParentComment(Long blogId, Long parentId) {
        if (parentId == null) {
            return;
        }
        TComment parent = commentMapper.selectById(parentId);
        if (parent == null || !Objects.equals(parent.getBlogId(), blogId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
    }

    private TComment getRequiredComment(Long id) {
        TComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        return comment;
    }
}
