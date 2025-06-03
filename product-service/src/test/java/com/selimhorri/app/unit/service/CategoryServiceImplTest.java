package com.selimhorri.app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.domain.Category;
import com.selimhorri.app.exception.wrapper.CategoryNotFoundException;
import com.selimhorri.app.helper.CategoryMappingHelper;
import com.selimhorri.app.repository.CategoryRepository;
import com.selimhorri.app.service.impl.CategoryServiceImpl;
import com.selimhorri.app.unit.util.CategoryUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryDto category;

    @BeforeEach
    void setUp() {
        category =  CategoryUtil.getSampleCategoryDto();
    }

    @Test
    void testFindById_ShouldReturnCategoryDto() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(CategoryUtil.getSampleCategory()));

        CategoryDto result = categoryService.findById(category.getCategoryId());

        assertNotNull(result);
        assertEquals(category.getCategoryId(), result.getCategoryId());
        assertEquals(category.getCategoryTitle(), result.getCategoryTitle());
        assertEquals(category.getImageUrl(), result.getImageUrl());
    }
}

