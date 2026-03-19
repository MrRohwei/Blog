package com.voidpen.server.module.category.service;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.category.model.request.CategoryQueryRequest;
import com.voidpen.server.module.category.model.request.CreateCategoryRequest;
import com.voidpen.server.module.category.model.request.UpdateCategoryRequest;
import com.voidpen.server.module.category.model.response.CategoryVO;
import java.util.List;

public interface CategoryService {

    List<CategoryVO> listEnabledCategories();

    PageResult<CategoryVO> listAdminCategories(CategoryQueryRequest request);

    void createCategory(CreateCategoryRequest request);

    void updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);
}
