package org.example.lab2.repository.inMemory;

import org.example.lab2.model.dto.CartDto;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryCartRepository {
    private final Map<String, CartDto> CartDtos = new ConcurrentHashMap<>();

    public CartDto save(CartDto CartDto) {
        CartDtos.put(CartDto.getUserId(), CartDto);
        return CartDto;
    }

    public Optional<CartDto> findByUserId(String userId) {
        return Optional.ofNullable(CartDtos.get(userId));
    }
}
