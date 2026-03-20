package com.voidpen.server.module.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.voidpen.server.common.constant.RedisKeys;
import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.blog.entity.TBlog;
import com.voidpen.server.module.blog.entity.TBlogTag;
import com.voidpen.server.module.blog.mapper.BlogMapper;
import com.voidpen.server.module.blog.mapper.BlogTagMapper;
import com.voidpen.server.module.blog.model.request.BlogQueryRequest;
import com.voidpen.server.module.blog.model.request.BlogSaveRequest;
import com.voidpen.server.module.blog.model.request.UpdateBlogStatusRequest;
import com.voidpen.server.module.blog.model.request.UpdateBlogTopRequest;
import com.voidpen.server.module.blog.model.response.ArchiveBlogItemVO;
import com.voidpen.server.module.blog.model.response.ArchiveVO;
import com.voidpen.server.module.blog.model.response.BlogDetailVO;
import com.voidpen.server.module.blog.model.response.BlogListVO;
import com.voidpen.server.module.blog.model.response.BlogTagRelationVO;
import com.voidpen.server.module.blog.model.response.BlogTagVO;
import com.voidpen.server.module.blog.service.BlogService;
import com.voidpen.server.module.blog.service.BlogViewAsyncService;
import com.voidpen.server.module.category.mapper.CategoryMapper;
import com.voidpen.server.module.tag.entity.TTag;
import com.voidpen.server.module.tag.mapper.TagMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private static final Integer STATUS_PUBLISHED = 1;

    private static final Integer FEATURED_LIMIT = 10;

    private final BlogMapper blogMapper;

    private final BlogTagMapper blogTagMapper;

    private final CategoryMapper categoryMapper;

    private final TagMapper tagMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    private final BlogViewAsyncService blogViewAsyncService;

    @Override
    public PageResult<BlogListVO> listPublicBlogs(BlogQueryRequest request) {
        return listBlogs(request, STATUS_PUBLISHED);
    }

    @Override
    public BlogDetailVO getPublicBlogDetail(Long id) {
        String cacheKey = RedisKeys.BLOG_DETAIL + id;
        Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
        if (cachedObj instanceof BlogDetailVO cached) {
            BlogDetailVO response = cached.copy();
            response.setViews(mergePendingViews(cached.getViews(), id));
            blogViewAsyncService.incrementViewAsync(id);
            return response;
        }

        BlogDetailVO detail = blogMapper.selectBlogDetailById(id);
        if (detail == null || !STATUS_PUBLISHED.equals(detail.getStatus())) {
            throw new BusinessException(ErrorCode.BLOG_NOT_FOUND);
        }

        detail.setTags(buildTagList(blogTagMapper.selectTagRelationsByBlogId(id)));
        redisTemplate.opsForValue().set(cacheKey, detail, 10, TimeUnit.MINUTES);

        BlogDetailVO response = detail.copy();
        response.setViews(mergePendingViews(detail.getViews(), id));
        blogViewAsyncService.incrementViewAsync(id);
        return response;
    }

    @Override
    public List<BlogListVO> listFeaturedBlogs() {
        List<BlogListVO> blogs = blogMapper.selectFeaturedBlogs(FEATURED_LIMIT);
        if (CollectionUtils.isEmpty(blogs)) {
            return blogs;
        }
        fillBlogTags(blogs);
        fillPendingViews(blogs);
        return blogs;
    }

    @Override
    public List<ArchiveVO> listArchiveBlogs() {
        List<ArchiveBlogItemVO> items = blogMapper.selectArchiveBlogItems();
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }

        Map<String, ArchiveVO> archiveMap = new LinkedHashMap<>();
        for (ArchiveBlogItemVO item : items) {
            LocalDateTime createdAt = item.getCreatedAt();
            String key = createdAt.getYear() + "-" + createdAt.getMonthValue();
            ArchiveVO archive = archiveMap.computeIfAbsent(key, k -> {
                ArchiveVO vo = new ArchiveVO();
                vo.setYear(createdAt.getYear());
                vo.setMonth(createdAt.getMonthValue());
                vo.setCount(0L);
                return vo;
            });
            archive.getBlogs().add(item);
            archive.setCount((long) archive.getBlogs().size());
        }

        return new ArrayList<>(archiveMap.values());
    }

    @Override
    public void likeBlog(Long blogId, String clientIp) {
        TBlog blog = getRequiredBlog(blogId);
        if (!STATUS_PUBLISHED.equals(blog.getStatus())) {
            throw new BusinessException(ErrorCode.BLOG_NOT_FOUND);
        }

        String likeKey = buildLikeKey(blogId, clientIp);
        Boolean alreadyLiked = redisTemplate.hasKey(likeKey);
        if (Boolean.TRUE.equals(alreadyLiked)) {
            throw new BusinessException(ErrorCode.BLOG_ALREADY_LIKED);
        }

        redisTemplate.opsForValue().set(likeKey, "1", 2, TimeUnit.DAYS);
        blogMapper.incrementLikes(blogId);
        redisTemplate.delete(RedisKeys.BLOG_DETAIL + blogId);
    }

    @Override
    public PageResult<BlogListVO> listAdminBlogs(BlogQueryRequest request) {
        return listBlogs(request, request.getStatus());
    }

    @Override
    public BlogDetailVO getAdminBlogDetail(Long id) {
        BlogDetailVO detail = blogMapper.selectBlogDetailById(id);
        if (detail == null) {
            throw new BusinessException(ErrorCode.BLOG_NOT_FOUND);
        }
        detail.setTags(buildTagList(blogTagMapper.selectTagRelationsByBlogId(id)));
        detail.setViews(mergePendingViews(detail.getViews(), id));
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBlog(Long userId, BlogSaveRequest request) {
        validateCategoryAndTags(request.getCategoryId(), request.getTagIds());

        TBlog blog = new TBlog()
            .setUserId(userId)
            .setCategoryId(request.getCategoryId())
            .setTitle(request.getTitle())
            .setContent(request.getContent())
            .setCoverImg(request.getCoverImg())
            .setSummary(request.getSummary())
            .setStatus(request.getStatus() == null ? 0 : request.getStatus())
            .setIsTop(request.getIsTop() == null ? 0 : request.getIsTop())
            .setIsFeatured(request.getIsFeatured() == null ? 0 : request.getIsFeatured())
            .setViews(0)
            .setLikes(0);
        blogMapper.insert(blog);

        refreshBlogTags(blog.getId(), request.getTagIds());
        redisTemplate.delete(RedisKeys.BLOG_DETAIL + blog.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBlog(Long id, BlogSaveRequest request) {
        TBlog existing = getRequiredBlog(id);
        validateCategoryAndTags(request.getCategoryId(), request.getTagIds());

        TBlog blog = new TBlog()
            .setId(id)
            .setUserId(existing.getUserId())
            .setCategoryId(request.getCategoryId())
            .setTitle(request.getTitle())
            .setContent(request.getContent())
            .setCoverImg(request.getCoverImg())
            .setSummary(request.getSummary())
            .setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus())
            .setIsTop(request.getIsTop() == null ? existing.getIsTop() : request.getIsTop())
            .setIsFeatured(request.getIsFeatured() == null ? existing.getIsFeatured() : request.getIsFeatured());
        blogMapper.updateById(blog);

        refreshBlogTags(id, request.getTagIds());
        redisTemplate.delete(RedisKeys.BLOG_DETAIL + id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBlog(Long id) {
        getRequiredBlog(id);
        blogTagMapper.deleteByBlogId(id);
        blogMapper.deleteById(id);
        redisTemplate.delete(RedisKeys.BLOG_DETAIL + id);
        redisTemplate.delete(RedisKeys.BLOG_VIEWS + id);
    }

    @Override
    public void updateBlogStatus(Long id, UpdateBlogStatusRequest request) {
        getRequiredBlog(id);
        blogMapper.updateById(new TBlog().setId(id).setStatus(request.getStatus()));
        redisTemplate.delete(RedisKeys.BLOG_DETAIL + id);
    }

    @Override
    public void updateBlogTop(Long id, UpdateBlogTopRequest request) {
        getRequiredBlog(id);
        blogMapper.updateById(new TBlog().setId(id).setIsTop(request.getIsTop()));
        redisTemplate.delete(RedisKeys.BLOG_DETAIL + id);
    }

    private PageResult<BlogListVO> listBlogs(BlogQueryRequest request, Integer status) {
        Page<BlogListVO> page = new Page<>(request.getPage(), request.getSize());
        IPage<BlogListVO> pageData = blogMapper.selectBlogPage(
            page,
            status,
            request.getCategoryId(),
            request.getTagId(),
            request.getKeyword()
        );
        fillBlogTags(pageData.getRecords());
        fillPendingViews(pageData.getRecords());
        return PageResult.of(pageData);
    }

    private void fillBlogTags(List<BlogListVO> blogs) {
        if (CollectionUtils.isEmpty(blogs)) {
            return;
        }

        List<Long> blogIds = blogs.stream().map(BlogListVO::getId).filter(Objects::nonNull).toList();
        if (CollectionUtils.isEmpty(blogIds)) {
            return;
        }

        List<BlogTagRelationVO> relations = blogTagMapper.selectTagRelationsByBlogIds(blogIds);
        if (CollectionUtils.isEmpty(relations)) {
            blogs.forEach(blog -> blog.setTags(new ArrayList<>()));
            return;
        }
        Map<Long, List<BlogTagVO>> tagMap = relations
            .stream()
            .collect(
                Collectors.groupingBy(
                    BlogTagRelationVO::getBlogId,
                    Collectors.mapping(this::toBlogTagVO, Collectors.toList())
                )
            );

        blogs.forEach(blog -> blog.setTags(tagMap.getOrDefault(blog.getId(), new ArrayList<>())));
    }

    private List<BlogTagVO> buildTagList(List<BlogTagRelationVO> relations) {
        if (CollectionUtils.isEmpty(relations)) {
            return new ArrayList<>();
        }
        return relations.stream().map(this::toBlogTagVO).collect(Collectors.toList());
    }

    private BlogTagVO toBlogTagVO(BlogTagRelationVO relation) {
        BlogTagVO tagVO = new BlogTagVO();
        tagVO.setId(relation.getTagId());
        tagVO.setName(relation.getTagName());
        tagVO.setColor(relation.getTagColor());
        return tagVO;
    }

    private void fillPendingViews(List<BlogListVO> blogs) {
        if (CollectionUtils.isEmpty(blogs)) {
            return;
        }
        blogs.forEach(blog -> blog.setViews(mergePendingViews(blog.getViews(), blog.getId())));
    }

    private Integer mergePendingViews(Integer dbViews, Long blogId) {
        int base = dbViews == null ? 0 : dbViews;
        long pending = parseLong(redisTemplate.opsForValue().get(RedisKeys.BLOG_VIEWS + blogId));
        long merged = base + pending;
        return merged > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) merged;
    }

    private long parseLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private void refreshBlogTags(Long blogId, List<Long> tagIds) {
        blogTagMapper.deleteByBlogId(blogId);
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<TBlogTag> relations = tagIds
            .stream()
            .filter(Objects::nonNull)
            .distinct()
            .map(tagId -> new TBlogTag(blogId, tagId))
            .toList();
        if (!CollectionUtils.isEmpty(relations)) {
            blogTagMapper.batchInsert(relations);
        }
    }

    private void validateCategoryAndTags(Long categoryId, List<Long> tagIds) {
        if (categoryId == null || categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<Long> distinctTagIds = tagIds.stream().filter(Objects::nonNull).distinct().toList();
        if (CollectionUtils.isEmpty(distinctTagIds)) {
            return;
        }

        List<TTag> tags = tagMapper.selectBatchIds(distinctTagIds);
        if (tags.size() != distinctTagIds.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
    }

    private TBlog getRequiredBlog(Long id) {
        TBlog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw new BusinessException(ErrorCode.BLOG_NOT_FOUND);
        }
        return blog;
    }

    private String buildLikeKey(Long blogId, String clientIp) {
        String ip = StringUtils.hasText(clientIp) ? clientIp : "unknown";
        return RedisKeys.BLOG_LIKE + blogId + ":" + LocalDate.now() + ":" + ip;
    }
}
