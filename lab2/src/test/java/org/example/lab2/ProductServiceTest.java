package org.example.lab2;

import jakarta.persistence.EntityNotFoundException;
import org.example.lab2.model.entity.Product;
import org.example.lab2.repository.ProductRepository;
import org.example.lab2.service.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .build();
    }

    @Test
    void getById_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.getById(1L);

        assertEquals(testProduct, result);
        verify(productRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productService.getById(999L),
                "Product not found");
    }

    @Test
    void create_ShouldSaveNewProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.create(testProduct);

        assertEquals(testProduct, result);
        verify(productRepository).save(testProduct);
    }

    @Test
    void update_ShouldUpdateExistingProduct() {
        Product updated = Product.builder()
                .name("Updated")
                .price(BigDecimal.valueOf(200))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        Product result = productService.update(1L, updated);

        assertEquals("Updated", result.getName());
        assertEquals(200, result.getPrice().intValue());
    }

    @Test
    void delete_ShouldCallRepository() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }
}

