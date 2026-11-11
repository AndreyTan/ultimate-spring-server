package org.example.rest.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime createdAt;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }


}