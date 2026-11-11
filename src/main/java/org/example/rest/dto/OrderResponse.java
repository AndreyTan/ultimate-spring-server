package org.example.rest.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.entity.Order;

import java.time.LocalDateTime;

@Setter
@Getter
public class OrderResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private LocalDateTime orderDate;
    private String status;

    public static OrderResponse fromEntity(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setProductId(order.getProduct().getId());
        dto.setQuantity(order.getQuantity());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus().name());
        return dto;
    }


}