package com.voidpen.server.module.category.controller;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.category.model.request.CategoryQueryRequest;
import com.voidpen.server.module.category.model.request.CreateCategoryRequest;
import com.voidpen.server.module.category.model.request.UpdateCategoryRequest;
import com.voidpen.server.module.category.model.response.CategoryVO;
import com.voidpen.server.module.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "分类管理（后台）")
@RestController
@RequestMapping("/admin/v1/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分页查询分类")
    @GetMapping
    public Result<PageResult<CategoryVO>> listAdminCategories(@Valid CategoryQueryRequest request) {
        return Result.success(categoryService.listAdminCategories(request));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Void> createCategory(@RequestBody @Valid CreateCategoryRequest request) {
        categoryService.createCategory(request);
        return Result.success();
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody @Valid UpdateCategoryRequest request) {
        categoryService.updateCategory(id, request);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
