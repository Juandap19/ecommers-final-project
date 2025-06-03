package com.selimhorri.app.unit.util;

import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.domain.Category;

import java.util.Arrays;
import java.util.List;

public class CategoryUtil {

    public static CategoryDto getSampleCategoryDto() {
        return CategoryDto.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("http://example.com/electronics.jpg")
                .build();
    }

    public static Category getSampleCategory() {
        Category category = new Category();
        category.setCategoryId(1);
        category.setCategoryTitle("Electronics");
        category.setImageUrl("http://example.com/electronics.jpg");
        return category;
    }

    public static List<Category> getSampleCategories() {
        return Arrays.asList(getSampleCategory());
    }
}
