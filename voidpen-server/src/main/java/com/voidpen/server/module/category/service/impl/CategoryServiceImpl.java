package com.voidpen.server.module.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.category.entity.TCategory;
import com.voidpen.server.module.category.mapper.CategoryMapper;
import com.voidpen.server.module.category.model.request.CategoryQueryRequest;
import com.voidpen.server.module.category.model.request.CreateCategoryRequest;
import com.voidpen.server.module.category.model.request.UpdateCategoryRequest;
import com.voidpen.server.module.category.model.response.CategoryVO;
import com.voidpen.server.module.category.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> listEnabledCategories() {
        return categoryMapper.selectEnabledWithBlogCount();
    }

    @Override
    public PageResult<CategoryVO> listAdminCategories(CategoryQueryRequest request) {
        Page<CategoryVO> page = new Page<>(request.getPage(), request.getSize());
        IPage<CategoryVO> pageData = categoryMapper.selectAdminPageWithBlogCount(page, request.getKeyword());
        return PageResult.of(pageData);
    }

    @Override
    public void createCategory(CreateCategoryRequest request) {
        checkDuplicateName(request.getName(), null);
        TCategory category = new TCategory()
            .setName(request.getName())
            .setDescription(request.getDescription())
            .setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
            .setStatus(request.getStatus() == null ? 1 : request.getStatus());
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long id, UpdateCategoryRequest request) {
        TCategory existing = getRequiredCategory(id);
        checkDuplicateName(request.getName(), id);
        TCategory toUpdate = new TCategory()
            .setId(id)
            .setName(request.getName())
            .setDescription(request.getDescription())
            .setSortOrder(request.getSortOrder() == null ? existing.getSortOrder() : request.getSortOrder())
            .setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
        categoryMapper.updateById(toUpdate);
    }

    @Override
    public void deleteCategory(Long id) {
        getRequiredCategory(id);
        Long blogCount = categoryMapper.countBlogsByCategoryId(id);
        if (blogCount != null && blogCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_BLOGS);
        }
        categoryMapper.deleteById(id);
    }

    private TCategory getRequiredCategory(Long id) {
        TCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return category;
    }

    private void checkDuplicateName(String name, Long excludeId) {
        LambdaQueryWrapper<TCategory> wrapper = new LambdaQueryWrapper<TCategory>().eq(TCategory::getName, name);
        if (excludeId != null) {
            wrapper.ne(TCategory::getId, excludeId);
        }
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_DUPLICATE);
        }
    }
}
