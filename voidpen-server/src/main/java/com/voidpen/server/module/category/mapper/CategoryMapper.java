package com.voidpen.server.module.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voidpen.server.module.category.entity.TCategory;
import com.voidpen.server.module.category.model.response.CategoryVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryMapper extends BaseMapper<TCategory> {

    IPage<CategoryVO> selectAdminPageWithBlogCount(IPage<?> page, @Param("keyword") String keyword);

    List<CategoryVO> selectEnabledWithBlogCount();

    Long countBlogsByCategoryId(@Param("categoryId") Long categoryId);
}
