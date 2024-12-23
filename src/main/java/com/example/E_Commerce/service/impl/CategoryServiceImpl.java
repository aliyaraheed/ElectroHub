package com.example.E_Commerce.service.impl;

import com.example.E_Commerce.model.Category;
import com.example.E_Commerce.repository.CategoryRepository;
import com.example.E_Commerce.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategory(){
        return categoryRepository.findAll();
    }

    @Override
    public Boolean deleteCategory(int id) {
        Category category = categoryRepository.findById(id).orElse(null);

        if(!ObjectUtils.isEmpty(category)){
            categoryRepository.delete(category);
            return true;
        }
        return false;
    }


    @Override
    public Boolean existCategory(String name) {

        return categoryRepository.existsByName(name);
    }

    @Override
    public Category getCategoryById(int id) {
        Category category = categoryRepository.findById(id).orElse(null);
        return category;
    }

    @Override
    public List<Category> getAllActiveCategory() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return categories;
    }


}