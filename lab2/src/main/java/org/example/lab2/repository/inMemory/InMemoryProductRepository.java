package org.example.lab2.repository.inMemory;

import org.example.lab2.model.dto.ProductDto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryProductRepository {
    private final Map<String, ProductDto> ProductDtos = new ConcurrentHashMap<>();

    public ProductDto save(ProductDto ProductDto) {
        ProductDtos.put(ProductDto.getId(), ProductDto);
        return ProductDto;
    }

    public List<ProductDto> findAll() {
        return new ArrayList<>(ProductDtos.values());
    }
}
