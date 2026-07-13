package org.example.repository;

import org.example.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Правильный метод для поиска по ID пользователя через связь
    List<Order> findByUser_Id(Long userId);

    // Правильный метод для поиска по ID продукта через связь
    List<Order> findByProduct_Id(Long productId);
}