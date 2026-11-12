package com.ultimateSpring.tests;

import com.ultimateSpring.base.BaseTest;
import io.restassured.http.ContentType;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.rest.dto.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserApiTest extends BaseTest {
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /users/{id} - Успешное получение пользователя")
    void getUserById() {
        // 1. Arrange (Подготовка): Сохраняем пользователя напрямую в БД
        User user = User.builder()
                .name("Roman222")
                .email("roman222@mail.com")
                .build();
        user = userRepository.save(user);

        // 2. Act & Assert (Действие и проверка): Идем в GET-ручку по ID
        given()
                .when()
                .get("/users/{id}", user.getId()) // Передаем ID из БД
                .then()
                .statusCode(200)
                .body("id", equalTo(user.getId().intValue()))
                .body("name", equalTo("Roman222"))
                .body("email", equalTo("roman222@mail.com"));
    }

    @Test
    @DisplayName("POST /users - Успешное создание пользователя")
    void shouldCreateUserSuccessfully() {
        String uniqueEmail = UUID.randomUUID() + "@gmail.com";

        UserRequest request = new UserRequest("chelovek001", uniqueEmail);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/users")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(request.getName()))
                .body("email", equalTo(request.getEmail()));
    }


    @Test
    @DisplayName("E2E: Создание и получение пользователя")
    void createAndGetUserFlowTest() {
        UserRequest request = new UserRequest("E2E Тест", UUID.randomUUID() + "@mail.com");

        // 1. Создаем (POST) и извлекаем ID
        Integer userId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract().path("id");

        // 2. Получаем (GET) по этому ID
        given()
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(200)
                .body("id", equalTo(userId))
                .body("name", equalTo(request.getName()))
                .body("email", equalTo(request.getEmail()));
    }



    @Test
    @DisplayName("GET /users/{id} - 404 когда пользователя нет")
    void shouldReturn404WhenUserNotFound() {
        given()
                .when()
                .get("/users/999999")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("message", containsString("User not found with id: 999999"));
    }

    @Test
    @DisplayName("POST /users - 400 при невалидном email")
    void shouldReturn400WhenEmailIsInvalid() {
        String invalidJson = """
        {
            "name": "Ivan",
            "email": "not-an-email"
        }
        """;

        given()
                .contentType("application/json")
                .body(invalidJson)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("errors.email", notNullValue()); // Проверяем, что ошибка именно по полю email
    }

    @Test
    @DisplayName("POST /users - 400 при попытке создать дубликат email")
    void shouldReturn400WhenEmailAlreadyExists() {
        UserRequest request = new UserRequest("Ivan", "duplicate@mail.com");

        // 1. Создаем первого пользователя (успешно)
        given()
                .contentType("application/json")
                .body(request)
                .post("/users")
                .then()
                .statusCode(201);

        // 2. Пытаемся создать второго с тем же email (должна быть ошибка 400 от BusinessException)
        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body("message", equalTo("Email already exists: duplicate@mail.com"));
    }

}
