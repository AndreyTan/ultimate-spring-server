package org.example.graphql.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ProductInput {
    private String name;
    private BigDecimal price;
    private Integer stock;


}