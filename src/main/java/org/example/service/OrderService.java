package org.example.service;

import org.example.entity.Order;
import org.example.entity.Product;
import org.example.entity.User;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository,
                        UserService userService,
                        ProductService productService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order create(Long userId, Long productId, Integer quantity) {
        User user = userService.findById(userId);
        Product product = productService.findById(productId);

        productService.decreaseStock(productId, quantity);

        Order order = new Order(user, product, quantity);
        return orderRepository.save(order);
    }

    public Order updateStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = findById(orderId);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public void cancelOrder(Long orderId) {
        Order order = findById(orderId);
        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel already delivered order");
        }
        productService.increaseStock(order.getProduct().getId(), order.getQuantity());
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }


    public List<Order> getOrdersByUser(Long userId) {
        userService.findById(userId); // проверяем существование пользователя
        List<Order> orders = orderRepository.findByUser_Id(userId);
        return orders != null ? orders : Collections.emptyList();
    }

    public List<Order> getOrdersByProduct(Long productId) {
        productService.findById(productId);
        List<Order> orders = orderRepository.findByProduct_Id(productId);
        return orders != null ? orders : Collections.emptyList();
    }
}