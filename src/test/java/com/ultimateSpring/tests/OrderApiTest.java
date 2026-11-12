package com.ultimateSpring.tests;

import com.ultimateSpring.base.BaseTest;
import org.example.entity.Product;
import org.example.entity.User;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.example.rest.dto.OrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderApiTest extends BaseTest {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/orders - Успешное создание заказа и списание стока")
    void shouldCreateOrderAndDecreaseStock() {
        User user = userRepository.save(new User("Test", UUID.randomUUID() + "@mail.com"));
        Product laptop = productRepository.save(new Product("Ноутбук", new BigDecimal(25000), 10));

        OrderRequest request = OrderRequest.builder()
                .userId(user.getId())
                .productId(laptop.getId())
                .quantity(2)
                .build();

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/orders")
                .then()
                .statusCode(201)
                .body("status", equalTo("PENDING"))
                .body("quantity", equalTo(2))
                .body("productId", equalTo(laptop.getId().intValue()));

        Product updatedProduct = productRepository.findById(laptop.getId()).orElseThrow();
        assertEquals(8, updatedProduct.getStock(), "Сток должен уменьшиться на 2");
    }

    @Test
    @DisplayName("POST /api/orders - 404 если пользователя не существует")
    void shouldReturn404WhenCreatingOrderForMissingUser() {
        String orderJson = """
        {
            "userId": 999999,
            "productId": 1,
            "quantity": 1
        }
        """;

        given()
                .contentType("application/json")
                .body(orderJson)
                .when()
                .post("/orders")
                .then()
                .statusCode(404)
                .body("message", containsString("User not found with id: 999999"));
    }

    @Test
    @DisplayName("POST /api/orders - 400 если товара не хватает на складе")
    void shouldReturn400WhenNotEnoughStock() {
        // 1. Готовим данные: создаем пользователя и товар (всего 2 штуки)
        User user = userRepository.save(new User("Test", UUID.randomUUID() + "@mail.com"));
        Product product = productRepository.save(new Product("Ноутбук", new BigDecimal(1000), 2));

        String orderJson = String.format("""
        {
            "userId": %d,
            "productId": %d,
            "quantity": 5
        }
        """, user.getId(), product.getId());

        // 2. Пытаемся заказать 5 штук (должна вылететь BusinessException)
        given()
                .contentType("application/json")
                .body(orderJson)
                .when()
                .post("/orders")
                .then()
                .statusCode(400)
                .body("message", containsString("Not enough stock. Available: 2"));
    }
}
