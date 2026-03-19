package com.voidpen.server.module.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voidpen.server.module.blog.entity.TBlog;
import com.voidpen.server.module.blog.model.response.ArchiveBlogItemVO;
import com.voidpen.server.module.blog.model.response.BlogDetailVO;
import com.voidpen.server.module.blog.model.response.BlogListVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BlogMapper extends BaseMapper<TBlog> {

    IPage<BlogListVO> selectBlogPage(
        IPage<?> page,
        @Param("status") Integer status,
        @Param("categoryId") Long categoryId,
        @Param("tagId") Long tagId,
        @Param("keyword") String keyword
    );

    BlogDetailVO selectBlogDetailById(@Param("id") Long id);

    List<BlogListVO> selectFeaturedBlogs(@Param("limit") Integer limit);

    int incrementViews(@Param("blogId") Long blogId, @Param("increment") Long increment);

    int incrementLikes(@Param("blogId") Long blogId);

    List<ArchiveBlogItemVO> selectArchiveBlogItems();
}
