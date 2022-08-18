package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfo=http://localhost:8084/v1/movieInfos",
                "restClient.reviews=http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerUnitTest {

    @Autowired
    WebTestClient webClient;

    @Test
    void movieIdTest() {
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieInfos/"+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBodyFile("movieInfo.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBodyFile("reviews.json")));

        webClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() ==2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });

    }

    @Test
    void movieIdTest_404() {
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieInfos/"+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withStatus(404)));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBodyFile("reviews.json")));

        webClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(errorMessage -> {
                    assert errorMessage != null;
                });

        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieInfos/"+movieId)));

    }

    @Test
    void movieIdTest_500() {
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieInfos/"+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withStatus(500)
                        .withBody("MovieInfo service error")));

        webClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class);

    }

    @Test
    void movieIdTest_500_retry() {
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieInfos/"+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withStatus(500)
                        .withBody("MovieInfo service error")));

        webClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .consumeWith(response -> {
                    var mesg = response.getResponseBody();
                    assert mesg != null;
                    assertEquals("MovieInfo service error", mesg);
                });

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieInfos/"+movieId)));
    }
}
