package org.example.lab2.service.product;

import lombok.RequiredArgsConstructor;
import org.example.lab2.model.dto.ProductDto;
import org.example.lab2.repository.inMemory.InMemoryProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final InMemoryProductRepository productRepository;

    public ProductDto createProduct(ProductDto product) {
        return productRepository.save(product);
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll();
    }
}
