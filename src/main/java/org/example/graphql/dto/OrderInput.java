package org.example.graphql.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderInput {
    private Long userId;
    private Long productId;
    private Integer quantity;


}