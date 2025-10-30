package com.example.stockservice.service.category;

import com.example.stockservice.model.Category;
import com.example.stockservice.repository.CategoryRepository;
import com.example.stockservice.request.category.CategoryCreateRequest;
import com.example.stockservice.request.category.CategoryUpdateRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public Category createCategory(CategoryCreateRequest request) {
        return categoryRepository.save(
                Category.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                        .build()
        );
    }

    @Override
    public Category updateCategory(CategoryUpdateRequest request) {
        Category toUpdate = findCategoryById(request.getId());
        modelMapper.map(request, toUpdate);
        return categoryRepository.save(toUpdate);
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(String id) {
        return findCategoryById(id);
    }

    @Override
    public Category findCategoryById(String id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")
        );
    }

    @Override
    public void deleteCategory(String id) {
        categoryRepository.deleteById(id);
    }
}
