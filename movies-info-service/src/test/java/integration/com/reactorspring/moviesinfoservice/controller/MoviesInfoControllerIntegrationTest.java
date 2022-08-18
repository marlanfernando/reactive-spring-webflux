package com.reactorspring.moviesinfoservice.controller;

import com.reactorspring.moviesinfoservice.domain.MovieInfo;
import com.reactorspring.moviesinfoservice.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    private static final String MOVIE_INFO_URL = "/v1/movieInfos";

    @BeforeEach
    void setUp() {
        var list = List.of(new MovieInfo(null, "Batman bigins", 2005, List.of("Christian Bale", "Michael cane"), LocalDate.parse("2005-04-23")),
                new MovieInfo(null, "Ironman", 2008, List.of("Rober Downey", "Michael worne"), LocalDate.parse("2008-11-23")),
                new MovieInfo("smnwh", "Spider-man no way home", 2022, List.of("Tom Holland", "Bennadict cumberbatch"), LocalDate.parse("2022-04-23")));

        movieInfoRepository.saveAll(list)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {

        var newMovie = new MovieInfo(null, "Ironman 2", 2012, List.of("Rober Downey", "Michael worne"), LocalDate.parse("2008-11-23"));


                webTestClient.post()
                        .uri(MOVIE_INFO_URL)
                        .bodyValue(newMovie)
                        .exchange()
                        .expectStatus()
                        .isCreated()
                        .expectBody(MovieInfo.class)
                        .consumeWith(movieInfoEntityExchangeResult -> {
                            var movie =movieInfoEntityExchangeResult.getResponseBody();
                            assert movie != null;
                            assert movie.getMovieInfoId() != null;
                        });


    }

    @Test
    void testGetAll() {
        webTestClient.get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void testGetAllByYear() {

        var uri = UriComponentsBuilder.fromUriString(MOVIE_INFO_URL)
                        .queryParam("year",2008)
                        .buildAndExpand().toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void testGetMovieById() {
        webTestClient.get()
                .uri(MOVIE_INFO_URL+"/{id}", "smnwh")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var res = movieInfoEntityExchangeResult.getResponseBody();
                    assert res != null;
                });

        // with json Path accessing the variable returns from the endpoint
        webTestClient.get()
                .uri(MOVIE_INFO_URL+"/{id}", "smnwh")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Spider-man no way home");
    }

    @Test
    void testGetMovieById_notFound() {
        webTestClient.get()
                .uri(MOVIE_INFO_URL+"/{id}", "not_found")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void updateMovieTest() {

        var updateMovie = new MovieInfo(null, "Ironman 3", 2012, List.of("Rober Downey", "Michael worne"), LocalDate.parse("2008-11-23"));

        webTestClient.put()
                .uri(MOVIE_INFO_URL+"/{id}", "smnwh")
                .bodyValue(updateMovie )
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var res = movieInfoEntityExchangeResult.getResponseBody();
                    assert res != null;
                    assertEquals("Ironman 3", res.getName());
                });

    }

    @Test
    void updateMovieTest_NotFound() {

        var updateMovie = new MovieInfo(null, "Ironman 3", 2012, List.of("Rober Downey", "Michael worne"), LocalDate.parse("2008-11-23"));

        webTestClient.put()
                .uri(MOVIE_INFO_URL+"/{id}", "not_found")
                .bodyValue(updateMovie )
                .exchange()
                .expectStatus()
                .isNotFound();

    }

    @Test
    void deleteTest(){
        webTestClient.delete()
                .uri(MOVIE_INFO_URL+"/{id}", "smnwh")
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}