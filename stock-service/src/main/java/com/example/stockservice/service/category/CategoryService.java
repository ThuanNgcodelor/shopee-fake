package com.example.stockservice.service.category;

import com.example.stockservice.model.Category;
import com.example.stockservice.request.category.CategoryCreateRequest;
import com.example.stockservice.request.category.CategoryUpdateRequest;

import java.util.List;

public interface CategoryService {
    Category createCategory(CategoryCreateRequest request);
    Category updateCategory(CategoryUpdateRequest request);
    List<Category> getAll();
    Category getCategoryById(String id);
    Category  findCategoryById(String id);
    void deleteCategory(String id);
}
