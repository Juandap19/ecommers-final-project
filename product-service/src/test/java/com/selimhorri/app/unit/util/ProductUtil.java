package com.selimhorri.app.unit.util;

import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.ProductDto;

import java.util.Arrays;
import java.util.List;

public class ProductUtil {

    public static ProductDto getSampleProductDto() {
        return ProductDto.builder()
                .productId(1)
                .productTitle("Electronics")
                .imageUrl("http://example.com/electronics.jpg")
                .sku("SKU12345")
                .priceUnit(299.99)
                .quantity(10)
                .categoryDto(CategoryUtil.getSampleCategoryDto())
                .build();
    }

    public static Product getSampleProduct() {
        Product product = new Product();
        product.setProductId(1);
        product.setProductTitle("Electronics");
        product.setImageUrl("http://example.com/electronics.jpg");
        product.setSku("SKU12345");
        product.setPriceUnit(299.99);
        product.setQuantity(10);
        product.setCategory(CategoryUtil.getSampleCategory());
        return product;
    }

    public static List<Product> getSampleProducts() {
        return Arrays.asList(getSampleProduct());
    }

}
