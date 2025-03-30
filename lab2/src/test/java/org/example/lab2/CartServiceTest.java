package org.example.lab2;

import jakarta.persistence.EntityNotFoundException;
import org.example.lab2.model.entity.CartItem;
import org.example.lab2.model.entity.Product;
import org.example.lab2.repository.CartItemRepository;
import org.example.lab2.repository.ProductRepository;
import org.example.lab2.service.cart.CartService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.TEN)
                .build();

        testCartItem = CartItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(2)
                .build();
    }

    @Test
    void addToCart_ShouldSaveNewItem() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartItem result = cartService.addToCart(1L, 2);

        assertEquals(2, result.getQuantity());
        assertEquals("Test Product", result.getProduct().getName());
    }

    @Test
    void removeFromCart_ShouldThrowWhenNotFound() {
        when(cartItemRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> cartService.removeFromCart(999L),
                "Cart item not found");
    }

    @Test
    void updateQuantity_ShouldUpdateExistingItem() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem result = cartService.updateQuantity(1L, 5);

        assertEquals(5, result.getQuantity());
    }
}

