package com.reactorspring.moviesinfoservice.controller;

import com.reactorspring.moviesinfoservice.domain.MovieInfo;
import com.reactorspring.moviesinfoservice.service.MovieInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    private static final String MOVIE_INFO_URL =  "/v1/movieInfos";
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    MovieInfoService movieInfoService;


    @Test
    void testGetAllMovieInfos() {
        var list = List.of(new MovieInfo(null, "Batman bigins", 2005, List.of("Christian Bale", "Michael cane"), LocalDate.parse("2005-04-23")),
                new MovieInfo(null, "Ironman", 2008, List.of("Rober Downey", "Michael worne"), LocalDate.parse("2008-11-23")),
                new MovieInfo("smnwh", "Spider-man no way home", 2022, List.of("Tom Holland", "Bennadict cumberbatch"), LocalDate.parse("2022-04-23")));

        when(movieInfoService.getAllMovies()).thenReturn(Flux.fromIterable(list));

        webTestClient.get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void testGetMovieInfoById() {
        var movieInfo = new MovieInfo("smnwh",
                "Spider-man no way home",
                2022,
                List.of("Tom Holland", "Bennadict cumberbatch"),
                LocalDate.parse("2022-04-23"));

        when(movieInfoService.getMovieInfoById(Mockito.anyString())).thenReturn(Mono.just(movieInfo));

        webTestClient.get()
                .uri(MOVIE_INFO_URL+"/{id}", "smnwh")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var response = movieInfoEntityExchangeResult.getResponseBody();
                    assert response != null;
                    assertEquals("Spider-man no way home", response.getName());
                });
    }

    @Test
    void addMovieInfo() {
        var newMovie = new MovieInfo("mockID",
                "Ironman 2",
                2012,
                List.of("Rober Downey", "Michael worne"),
                LocalDate.parse("2008-11-23"));

        when(movieInfoService.save(isA(MovieInfo.class))).thenReturn(Mono.just(newMovie));

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
                    assertEquals("mockID", movie.getMovieInfoId());
                });

    }

    @Test
    void addMovieInfo_validations() {
        var newMovie = new MovieInfo("mockID",
                "",
                -2012,
                List.of(""),
                LocalDate.parse("2008-11-23"));

        webTestClient.post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(newMovie)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var response = stringEntityExchangeResult.getResponseBody();
                    assert response!=null;
                    assertEquals("movieInfo.cast should be present,movieInfo.name should not be blank,movieInfo.year should be a possitive integer", response);
                });

    }

    @Test
    void updateMovie() {

        var updateMovie = new MovieInfo(null,
                "Ironman 3",
                2012, List.of("Rober Downey", "Michael worne"),
                LocalDate.parse("2008-11-23"));

        when(movieInfoService.updateMovie(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(updateMovie));

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
    void deleteMovie() {

        when(movieInfoService.deleteMovie(isA(String.class))).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(MOVIE_INFO_URL+"/{id}", "smnwh")
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}