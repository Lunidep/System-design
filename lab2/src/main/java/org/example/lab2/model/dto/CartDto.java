package org.example.lab2.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class CartDto {
    private String userId;
    private List<CartItemDto> items = new ArrayList<>();
}