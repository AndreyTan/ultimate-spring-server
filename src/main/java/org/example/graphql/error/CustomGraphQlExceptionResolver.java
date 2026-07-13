package org.example.graphql.error;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
public class CustomGraphQlExceptionResolver implements DataFetcherExceptionResolver {

    @Override
    public Mono<List<GraphQLError>> resolveException(Throwable exception, DataFetchingEnvironment env) {
        GraphQLError error;

        if (exception instanceof ResourceNotFoundException) {
            error = GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.NOT_FOUND)
                    .message(exception.getMessage())
                    .extensions(Map.of(
                            "timestamp", OffsetDateTime.now().toString(),
                            "type", "NOT_FOUND"
                    ))
                    .build();
        } else if (exception instanceof BusinessException) {
            error = GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(exception.getMessage())
                    .extensions(Map.of(
                            "timestamp", OffsetDateTime.now().toString(),
                            "type", "BUSINESS_ERROR"
                    ))
                    .build();
        } else {
            // любая другая ошибка (внутренняя)
            error = GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .message("Internal server error")
                    .extensions(Map.of(
                            "timestamp", OffsetDateTime.now().toString(),
                            "traceId", java.util.UUID.randomUUID().toString()
                    ))
                    .build();
        }

        return Mono.just(List.of(error));
    }
}