package com.example.stockservice.controller;

import com.example.stockservice.dto.CategoryDto;
import com.example.stockservice.model.Category;
import com.example.stockservice.request.category.CategoryCreateRequest;
import com.example.stockservice.request.category.CategoryUpdateRequest;
import com.example.stockservice.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/stock/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final ModelMapper modelMapper;

    @GetMapping("/getAll")
    public ResponseEntity<List<CategoryDto>> getAll() {
        List<Category> categories = categoryService.getAll();
        List<CategoryDto> categoryDtos = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDto.class))
                .toList();
        return ResponseEntity.ok(categoryDtos);
    }

    @GetMapping("/getCategoryById/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable String id) {
        Category category = categoryService.getCategoryById(id);
        CategoryDto categoryDto = modelMapper.map(category, CategoryDto.class);
        return ResponseEntity.ok(categoryDto);
    }

    @PostMapping("/create")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        Category category = categoryService.createCategory(request);
        CategoryDto categoryDto = modelMapper.map(category, CategoryDto.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryDto);
    }

    @PutMapping("/update")
    public ResponseEntity<CategoryDto> updateCategory(@Valid @RequestBody CategoryUpdateRequest request) {
        Category category = categoryService.updateCategory(request);
        CategoryDto categoryDto = modelMapper.map(category, CategoryDto.class);
        return ResponseEntity.ok(categoryDto);
    }

    @DeleteMapping("/deleteCategoryById/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

