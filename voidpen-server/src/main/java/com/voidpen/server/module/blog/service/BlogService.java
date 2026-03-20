package com.voidpen.server.module.blog.service;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.blog.model.request.BlogQueryRequest;
import com.voidpen.server.module.blog.model.request.BlogSaveRequest;
import com.voidpen.server.module.blog.model.request.UpdateBlogStatusRequest;
import com.voidpen.server.module.blog.model.request.UpdateBlogTopRequest;
import com.voidpen.server.module.blog.model.response.ArchiveVO;
import com.voidpen.server.module.blog.model.response.BlogDetailVO;
import com.voidpen.server.module.blog.model.response.BlogListVO;
import java.util.List;

public interface BlogService {

    PageResult<BlogListVO> listPublicBlogs(BlogQueryRequest request);

    BlogDetailVO getPublicBlogDetail(Long id);

    List<BlogListVO> listFeaturedBlogs();

    List<ArchiveVO> listArchiveBlogs();

    void likeBlog(Long blogId, String clientIp);

    PageResult<BlogListVO> listAdminBlogs(BlogQueryRequest request);

    BlogDetailVO getAdminBlogDetail(Long id);

    void createBlog(Long userId, BlogSaveRequest request);

    void updateBlog(Long id, BlogSaveRequest request);

    void deleteBlog(Long id);

    void updateBlogStatus(Long id, UpdateBlogStatusRequest request);

    void updateBlogTop(Long id, UpdateBlogTopRequest request);
}
