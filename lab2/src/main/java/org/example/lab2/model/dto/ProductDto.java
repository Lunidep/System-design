package org.example.lab2.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class ProductDto {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
}
