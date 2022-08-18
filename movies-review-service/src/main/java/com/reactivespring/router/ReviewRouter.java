package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewsRouter(ReviewHandler reviewHandler) {
        return route()
                // nesting same uri with different http methods or endpoints
                .nest(path("/v1/reviews"), builder -> {
                    // no need to add the url pattern here if the patern is same as above, if its change like the one in
                    // PUT endpoint add the additional in patern
                    builder.POST("", reviewHandler::addReview)
                            .GET("", reviewHandler::getReviews)
                            .PUT("/{id}", reviewHandler::updateReview)
                            .DELETE("/{id}",reviewHandler::deleteReview)
                            .GET("/stream", reviewHandler::streamReview);
                })
                .GET("/v1/helloworld",(request ->  ServerResponse.ok().bodyValue("hello world")))
                .build();
    }
}
