# Ultimate Spring Server

**Многопротокольный сервер на Spring Boot** с поддержкой REST API, GraphQL и gRPC, использующий SQLite в качестве базы данных.

Этот проект демонстрирует построение современного веб-приложения с тремя различными способами взаимодействия, что позволяет гибко интегрироваться с разными типами клиентов (веб, мобильные приложения, микросервисы).

---

##  Возможности

- **REST API** – классический CRUD для управления пользователями, продуктами и заказами.
- **GraphQL API** – гибкие запросы и мутации с поддержкой вложенных данных.
- **gRPC API** – высокопроизводительный RPC-сервис для работы с заказами.
- **База данных** – SQLite (легковесная, не требует отдельного сервера).
- **Валидация и обработка ошибок** – единый подход для всех протоколов.
- **Документация** – примеры запросов для каждого API.

---

## ️ Технологический стек

| Компонент            | Технология                              |
|----------------------|-----------------------------------------|
| Фреймворк            | Spring Boot 4.1.0                       |
| Язык                 | Java 21                                 |
| База данных          | SQLite + Hibernate/JPA                  |
| REST                 | Spring Web MVC                          |
| GraphQL              | Spring for GraphQL                      |
| gRPC                 | Spring gRPC (официальный стартер)       |
| Сборка               | Maven 3.9+                              |
| Тестирование         | JUnit, Postman, grpcurl                 |

---

##  Структура базы данных

Проект использует три таблицы:

```sql
users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
)

products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
)

orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
)
```
---

##  Сборка и запуск
### Требования
Java 21+

Maven 3.9+

SQLite (не требуется установка, используется встроенная)

## Шаги

1. Клонируйте репозиторий
```bash
git clone https://github.com/AndreyTan/ultimate-spring-server
```
2. Соберите проект
```bash
mvn clean compile
```
3. Запустите приложение
```bash
mvn spring-boot:run
```
Или запустите класс org.example.Main в вашей IDE.

Приложение стартует на портах:

8081 – REST + GraphQL

9090 – gRPC

##  Использование API

## REST API

Базовый URL: http://localhost:8081/api

Пример: создание пользователя
```http
POST /users
Content-Type: application/json

{
  "name": "Alice",
  "email": "alice@example.com"
}
```
Пример: создание заказа
```http
POST /orders
Content-Type: application/json

{
  "userId": 1,
  "productId": 1,
  "quantity": 2
}
```
Полный список эндпоинтов:

- GET /users – список пользователей
- GET /users/{id} – получить пользователя
- PUT /users/{id} – обновить
- DELETE /users/{id} – удалить
- Аналогично для /products и /orders

## GraphQL API

Эндпоинт: http://localhost:8081/graphql

Для удобства включён GraphiQL – интерактивная среда: http://localhost:8081/graphiql

Пример запроса (получить пользователей с их заказами)
```graphql
{
  users {
    id
    name
    email
    orders {
      id
      productId
      quantity
      status
    }
  }
}
```

Пример мутации (создать заказ)
```graphql
mutation {
  createOrder(input: { userId: 1, productId: 1, quantity: 3 }) {
    id
    status
  }
}
```

Пример мутации (обновить статус заказа)
```graphql
mutation {
    updateOrderStatus(id: 1, status: "SHIPPED") {
        id
        status
    }
}
```

## gRPC API

Для вызова gRPC используйте Postman (gRPC-запрос) или утилиту grpcurl.

Сервер: grpc://localhost:9090

Сервис: com.example.grpc.order.OrderService

Доступные методы

| Метод                | Описание                           |
|----------------------|------------------------------------|
| CreateOrder          | Создать новый заказ                |
| GetOrder             | Получить заказ по ID               |
| ListOrders           | Получить все заказы (с фильтрами)  |
| UpdateOrderStatus    | Обновить статус заказа             |
| CancelOrder          | Отменить заказ (возврат товара)    |


