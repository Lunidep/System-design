package org.example.cartservice.service.cart;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.cartservice.model.entity.CartItem;
import org.example.cartservice.repository.CartItemRepository;
import org.example.productservice.model.entity.Product;
import org.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartItem addToCart(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        CartItem cartItem = CartItem.builder()
                .product(product)
                .quantity(quantity)
                .build();

        return cartItemRepository.save(cartItem);
    }

    public void removeFromCart(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new EntityNotFoundException("Cart item not found");
        }
        cartItemRepository.deleteById(cartItemId);
    }

    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
    }

    public CartItem updateQuantity(Long cartItemId, int quantity) {
        return cartItemRepository.findById(cartItemId)
                .map(cartItem -> {
                    cartItem.setQuantity(quantity);
                    return cartItemRepository.save(cartItem);
                })
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));
    }
}

