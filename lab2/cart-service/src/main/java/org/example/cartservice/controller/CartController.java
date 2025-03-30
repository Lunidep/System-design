package org.example.cartservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.cartservice.model.entity.CartItem;
import org.example.cartservice.service.cart.CartService;
import org.example.userservice.configuration.aspect.CustomAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @CustomAuthorize({"ROLE_USER", "ROLE_ADMIN"})
    @PostMapping
    public ResponseEntity<CartItem> addToCart(@RequestParam Long productId, @RequestParam int quantity) {
        return new ResponseEntity<>(cartService.addToCart(productId, quantity), HttpStatus.CREATED);
    }

    @CustomAuthorize({"ROLE_USER", "ROLE_ADMIN"})
    @DeleteMapping("/{cartItemId}/delete")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @CustomAuthorize({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping
    public ResponseEntity<List<CartItem>> getAllCartItems() {
        return ResponseEntity.ok(cartService.getAllCartItems());
    }

    @CustomAuthorize({"ROLE_USER", "ROLE_ADMIN"})
    @PutMapping("/{cartItemId}/quantity")
    public ResponseEntity<CartItem> updateQuantity(@PathVariable Long cartItemId, @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(cartItemId, quantity));
    }
}

