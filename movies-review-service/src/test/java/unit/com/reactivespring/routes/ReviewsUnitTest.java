package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.ExceptionHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration( classes = {ReviewRouter.class, ReviewHandler.class, ExceptionHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    WebTestClient webTestClient;

    private static final String REVIEWS_INFO = "/v1/reviews";

    @Test
    void addReviewTest() {

        var review = new Review("abcd", 1L,"Elah movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(review));

        webTestClient.post()
                .uri(REVIEWS_INFO)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var reviewInfo =reviewEntityExchangeResult.getResponseBody();
                    assert reviewInfo != null;
                    assert reviewInfo.getReviewId() != null;
                    assertEquals("abcd",reviewInfo.getReviewId());
                });

    }

    @Test
    void addReviewTest_validate() {

        var review = new Review("abcd", null,"Elah movie", -9.0);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(review));

        webTestClient.post()
                .uri(REVIEWS_INFO)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest();

    }

    @Test
    void allReviewTest() {

        var reviews = List.of(new Review("abcd", 1L,"Elah movie", 9.0));

        when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviews));

        webTestClient.get()
                .uri(REVIEWS_INFO)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(1);
    }

    @Test
    void updateReviewTest() {

        var reviewBeforeUpdate = new Review("test", 1L,"not so greate movie", 6.0);
        var updatedReview = new Review("test", 1L,"good movie", 9.0);

        when(reviewReactiveRepository.findById(isA(String.class))).thenReturn(Mono.just(reviewBeforeUpdate));
        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(updatedReview));

        webTestClient.put()
                .uri(REVIEWS_INFO+"/{id}", "test")
                .bodyValue(updatedReview)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var response = reviewEntityExchangeResult.getResponseBody();
                    assert response != null;
                    assertEquals("good movie", response.getComment());
                    assertEquals(9.0, response.getRating());

                });

    }

    @Test
    void updateReviewTest_404() {

        var updatedReview = new Review("test", 1L,"good movie", 9.0);

        when(reviewReactiveRepository.findById(isA(String.class))).thenReturn(Mono.empty());

        webTestClient.put()
                .uri(REVIEWS_INFO+"/{id}", "test")
                .bodyValue(updatedReview)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteTest() {

        when(reviewReactiveRepository.findById(isA(String.class))).thenReturn(Mono.just(new Review()));
        when(reviewReactiveRepository.delete(isA(Review.class))).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(REVIEWS_INFO+"/{id}","test")
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
