package org.example.graphql;

import org.example.entity.Order;
import org.example.entity.Product;
import org.example.entity.User;
import org.example.graphql.dto.OrderInput;
import org.example.graphql.dto.ProductInput;
import org.example.graphql.dto.UserInput;
import org.example.service.OrderService;
import org.example.service.ProductService;
import org.example.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class GraphQLController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    public GraphQLController(UserService userService,
                             ProductService productService,
                             OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
    }

    // ---------- QUERIES ----------

    // Users
    @QueryMapping
    public List<User> users() {
        return userService.findAll();
    }

    @QueryMapping
    public User user(@Argument Long id) {
        return userService.findById(id);
    }

    // Products
    @QueryMapping
    public List<Product> products() {
        return productService.findAll();
    }

    @QueryMapping
    public Product product(@Argument Long id) {
        return productService.findById(id);
    }

    // Orders
    @QueryMapping
    public List<Order> orders() {
        return orderService.findAll();
    }

    @QueryMapping
    public Order order(@Argument Long id) {
        return orderService.findById(id);
    }

    @QueryMapping
    public List<Order> ordersByUser(@Argument Long userId) {
        return orderService.getOrdersByUser(userId);
    }

    @QueryMapping
    public List<Order> ordersByProduct(@Argument Long productId) {
        return orderService.getOrdersByProduct(productId);
    }

    // ---------- MUTATIONS ----------

    // Users
    @MutationMapping
    public User createUser(@Argument UserInput input) {
        User user = new User(input.getName(), input.getEmail());
        return userService.create(user);
    }

    @MutationMapping
    public User updateUser(@Argument Long id, @Argument UserInput input) {
        User updated = new User(input.getName(), input.getEmail());
        return userService.update(id, updated);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        userService.delete(id);
        return true;
    }

    // Products
    @MutationMapping
    public Product createProduct(@Argument ProductInput input) {
        Product product = new Product(input.getName(), input.getPrice(), input.getStock());
        return productService.create(product);
    }

    @MutationMapping
    public Product updateProduct(@Argument Long id, @Argument ProductInput input) {
        Product updated = new Product(input.getName(), input.getPrice(), input.getStock());
        return productService.update(id, updated);
    }

    @MutationMapping
    public Boolean deleteProduct(@Argument Long id) {
        productService.delete(id);
        return true;
    }

    // Orders
    @MutationMapping
    public Order createOrder(@Argument OrderInput input) {
        return orderService.create(input.getUserId(), input.getProductId(), input.getQuantity());
    }

    @MutationMapping
    public Order updateOrderStatus(@Argument Long id, @Argument String status) {
        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        return orderService.updateStatus(id, newStatus);
    }

    @MutationMapping
    public Boolean cancelOrder(@Argument Long id) {
        orderService.cancelOrder(id);
        return true;
    }
}