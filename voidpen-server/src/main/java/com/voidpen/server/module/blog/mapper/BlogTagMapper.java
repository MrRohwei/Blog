package com.voidpen.server.module.blog.mapper;

import com.voidpen.server.module.blog.entity.TBlogTag;
import com.voidpen.server.module.blog.model.response.BlogTagRelationVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BlogTagMapper {

    int deleteByBlogId(@Param("blogId") Long blogId);

    int batchInsert(@Param("relations") List<TBlogTag> relations);

    List<BlogTagRelationVO> selectTagRelationsByBlogIds(@Param("blogIds") List<Long> blogIds);

    List<BlogTagRelationVO> selectTagRelationsByBlogId(@Param("blogId") Long blogId);
}
