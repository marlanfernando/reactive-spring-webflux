package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    private static final String REVIEWS_INFO = "/v1/reviews";

    @BeforeEach
    void setUp() {

        var setupList = List.of(new Review(null, 1L,"Elah movie", 9.0),
                new Review(null, 2L,"Elah movie 2", 8.0),
                new Review("test", 3L,"Elah movie 3", 8.9));

        reviewReactiveRepository.saveAll(setupList).blockLast();

    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {

        var review = new Review(null, 1L,"Elah movie", 9.0);

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
                });
    }

    @Test
    void getReviews() {

        webTestClient.get()
                .uri(REVIEWS_INFO)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateTest() {

        var updatedReview = new Review("test", 1L,"not so greate movie", 6.0);

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
                    assertEquals("not so greate movie", response.getComment());
                    assertEquals(6.0, response.getRating());

                });
    }

    @Test
    void deleteTest() {

        webTestClient.delete()
                .uri(REVIEWS_INFO+"/{id}","test")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void getByMovieInfoIdTest() {
        var uri = UriComponentsBuilder.fromUriString(REVIEWS_INFO)
                .queryParam("movieInfoId",1L)
                .buildAndExpand().toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(1);
    }
}
