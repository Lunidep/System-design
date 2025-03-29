package org.example.lab2.service.cart;

import lombok.RequiredArgsConstructor;
import org.example.lab2.model.dto.CartDto;
import org.example.lab2.model.dto.CartItemDto;
import org.example.lab2.repository.inMemory.InMemoryCartRepository;
import org.example.lab2.repository.inMemory.InMemoryProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final InMemoryCartRepository cartRepository;
    private final InMemoryProductRepository productRepository;

    public CartDto addToCart(String userId, CartItemDto item) {
        if (productRepository.findAll().stream()
                .noneMatch(p -> p.getId().equals(item.getProductId()))) {
            throw new IllegalArgumentException("Product not found");
        }

        CartDto cart = cartRepository.findByUserId(userId)
                .orElse(new CartDto());
        cart.setUserId(userId);

        cart.getItems().add(item);
        return cartRepository.save(cart);
    }

    public Optional<CartDto> getCart(String userId) {
        return cartRepository.findByUserId(userId);
    }
}
