package com.voidpen.server.module.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voidpen.server.module.tag.entity.TTag;
import com.voidpen.server.module.tag.model.response.TagVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TagMapper extends BaseMapper<TTag> {

    IPage<TagVO> selectAdminPageWithBlogCount(IPage<?> page, @Param("keyword") String keyword);

    List<TagVO> selectPublicListWithBlogCount();

    int deleteBlogTagRelationsByTagId(@Param("tagId") Long tagId);
}
