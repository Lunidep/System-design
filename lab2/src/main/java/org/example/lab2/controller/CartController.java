package org.example.lab2.controller;

import lombok.RequiredArgsConstructor;
import org.example.lab2.model.dto.CartDto;
import org.example.lab2.model.dto.CartItemDto;
import org.example.lab2.service.cart.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/{userId}/add")
    public ResponseEntity<CartDto> addToCart(
            @PathVariable String userId,
            @RequestBody CartItemDto item) {
        return ResponseEntity.ok(cartService.addToCart(userId, item));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartDto> getCart(@PathVariable String userId) {
        return cartService.getCart(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
