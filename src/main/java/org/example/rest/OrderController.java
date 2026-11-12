package org.example.rest;

import jakarta.validation.Valid;
import org.example.entity.Order;
import org.example.exception.BusinessException;
import org.example.rest.dto.OrderRequest;
import org.example.rest.dto.OrderResponse;
import org.example.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.findAll().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return OrderResponse.fromEntity(orderService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.create(request.getUserId(), request.getProductId(), request.getQuantity());
        return OrderResponse.fromEntity(order);
    }

    @PutMapping("/{id}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid order status: " + status); // Вернет 400
        }
        Order updated = orderService.updateStatus(id, newStatus);
        return OrderResponse.fromEntity(updated);
    }

    @PutMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUser(userId).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/product/{productId}")
    public List<OrderResponse> getOrdersByProduct(@PathVariable Long productId) {
        return orderService.getOrdersByProduct(productId).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }
}