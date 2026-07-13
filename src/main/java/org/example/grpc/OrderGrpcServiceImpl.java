package org.example.grpc;

import com.example.grpc.order.OrderOuterClass;
import com.example.grpc.order.OrderServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.entity.Order;
import org.example.entity.Order.OrderStatus;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

import java.time.format.DateTimeFormatter;
import java.util.List;

@GrpcService
public class OrderGrpcServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(OrderGrpcServiceImpl.class);
    private final OrderService orderService;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public OrderGrpcServiceImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    private OrderOuterClass.Order toProto(Order order) {
        return OrderOuterClass.Order.newBuilder()
                .setId(order.getId())
                .setUserId(order.getUser().getId())
                .setProductId(order.getProduct().getId())
                .setQuantity(order.getQuantity())
                .setOrderDate(order.getOrderDate().format(ISO_FORMATTER))
                .setStatus(order.getStatus().name())
                .build();
    }

    @Override
    public void createOrder(OrderOuterClass.CreateOrderRequest request,
                            StreamObserver<OrderOuterClass.Order> responseObserver) {
        log.info("Creating order: userId={}, productId={}, quantity={}",
                request.getUserId(), request.getProductId(), request.getQuantity());
        try {
            // Валидация
            if (request.getUserId() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("user_id must be positive")
                        .asRuntimeException();
            }
            if (request.getProductId() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("product_id must be positive")
                        .asRuntimeException();
            }
            if (request.getQuantity() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("quantity must be positive")
                        .asRuntimeException();
            }

            Order created = orderService.create(
                    request.getUserId(),
                    request.getProductId(),
                    request.getQuantity()
            );
            log.info("Order created with id={}", created.getId());
            responseObserver.onNext(toProto(created));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error creating order", e);
            // Преобразуем исключения в понятные gRPC статусы
            if (e instanceof ResourceNotFoundException) {
                responseObserver.onError(
                        Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
                );
            } else if (e instanceof BusinessException) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
                );
            } else {
                responseObserver.onError(
                        Status.INTERNAL.withDescription("Internal server error").asRuntimeException()
                );
            }
        }
    }

    @Override
    public void getOrder(OrderOuterClass.GetOrderRequest request,
                         StreamObserver<OrderOuterClass.Order> responseObserver) {
        try {
            Order order = orderService.findById(request.getId());
            responseObserver.onNext(toProto(order));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting order", e);
            if (e instanceof ResourceNotFoundException) {
                responseObserver.onError(
                        Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
                );
            } else {
                responseObserver.onError(
                        Status.INTERNAL.withDescription("Internal server error").asRuntimeException()
                );
            }
        }
    }

    @Override
    public void listOrders(OrderOuterClass.ListOrdersRequest request,
                           StreamObserver<OrderOuterClass.ListOrdersResponse> responseObserver) {
        try {
            List<Order> orders;
            if (request.getUserId() != 0) {
                orders = orderService.getOrdersByUser(request.getUserId());
            } else if (request.getProductId() != 0) {
                orders = orderService.getOrdersByProduct(request.getProductId());
            } else {
                orders = orderService.findAll();
            }
            OrderOuterClass.ListOrdersResponse response = OrderOuterClass.ListOrdersResponse.newBuilder()
                    .addAllOrders(orders.stream().map(this::toProto).toList())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error listing orders", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Internal server error").asRuntimeException()
            );
        }
    }

    @Override
    public void updateOrderStatus(OrderOuterClass.UpdateOrderStatusRequest request,
                                  StreamObserver<OrderOuterClass.Order> responseObserver) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus());
            Order updated = orderService.updateStatus(request.getId(), newStatus);
            responseObserver.onNext(toProto(updated));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error updating order status", e);
            if (e instanceof ResourceNotFoundException) {
                responseObserver.onError(
                        Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
                );
            } else if (e instanceof IllegalArgumentException) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT.withDescription("Invalid status: " + request.getStatus()).asRuntimeException()
                );
            } else {
                responseObserver.onError(
                        Status.INTERNAL.withDescription("Internal server error").asRuntimeException()
                );
            }
        }
    }

    @Override
    public void cancelOrder(OrderOuterClass.CancelOrderRequest request,
                            StreamObserver<OrderOuterClass.Empty> responseObserver) {
        try {
            orderService.cancelOrder(request.getId());
            responseObserver.onNext(OrderOuterClass.Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error cancelling order", e);
            if (e instanceof ResourceNotFoundException) {
                responseObserver.onError(
                        Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
                );
            } else if (e instanceof BusinessException) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
                );
            } else {
                responseObserver.onError(
                        Status.INTERNAL.withDescription("Internal server error").asRuntimeException()
                );
            }
        }
    }
}