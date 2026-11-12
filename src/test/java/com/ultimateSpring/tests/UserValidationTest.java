package com.ultimateSpring.tests;

import com.ultimateSpring.base.BaseTest;
import org.example.repository.UserRepository;
import org.example.rest.dto.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserValidationTest extends BaseTest {
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @ParameterizedTest(name = "Невалидный email: {0}")
    @ValueSource(strings = {"invalid-email", "test@", "@mail.com", "testmail.com", ""})
    void shouldRejectInvalidEmails(String invalidEmail) {
        String jsonBody = """
            {
                "name": "Ivan",
                "email": "%s"
            }
            """.formatted(invalidEmail);

        given()
                .contentType("application/json")
                .body(jsonBody)
                .when()
                .post("/users")
                .then()
                .statusCode(400);
    }

    @ParameterizedTest(name = "Имя: {0}, Email: {1}")
    @CsvSource({
            "Ivan, ivan@mail.com",
            "Petr, petr@gmail.com",
            "Анна, anna@yandex.ru",
            "John Doe, john.doe@company.org"
    })
    void shouldCreateUsersWithDifferentData(String name, String email) {
        UserRequest request = new UserRequest(name, email);

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("name", equalTo(name));
    }

    static Stream<Arguments> provideUsersForCreation() {
        return Stream.of(
                Arguments.of("User One", "one-" + UUID.randomUUID() + "@mail.com"),
                Arguments.of("User Two", "two-" + UUID.randomUUID() + "@mail.com"),
                Arguments.of("Юникод Имя", "unicode-" + UUID.randomUUID() + "@mail.com")
        );
    }

    @ParameterizedTest(name = "Создание пользователя: {0}")
    @MethodSource("provideUsersForCreation")
    void shouldCreateUserSuccessfully(String name, String email) {
        UserRequest request = new UserRequest(name, email);

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("email", equalTo(email));
    }

    @ParameterizedTest(name = "Невалидные данные: {0} -> ожидаем {2}")
    @MethodSource("invalidUsersProvider")
    void shouldRejectInvalidUsers(String name, String email, int expectedStatusCode) {
        UserRequest request = new UserRequest(name, email);

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(expectedStatusCode);
    }

    static Stream<Arguments> invalidUsersProvider() {
        return Stream.of(
                Arguments.of("", "valid@mail.com", 400), // Пустое имя -> Bad Request
                Arguments.of("Ivan", "", 400),           // Пустой email -> Bad Request
                Arguments.of("Ivan", "invalid-email", 400) // Невалидный email -> Bad Request
        );
    }

}
