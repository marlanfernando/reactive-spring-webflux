package com.reactivespring.controller;

import com.reactivespring.client.MovieInfoRestClient;
import com.reactivespring.client.ReviewRestClient;
import com.reactivespring.domain.Movie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
public class MoviesController {

    private MovieInfoRestClient movieInfoRestClient;
    private ReviewRestClient reviewRestClient;

    public MoviesController(MovieInfoRestClient movieInfoRestClient, ReviewRestClient reviewRestClient) {
        this.movieInfoRestClient = movieInfoRestClient;
        this.reviewRestClient = reviewRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> retriveMovieById(@PathVariable("id") String movieId) {

        return movieInfoRestClient.retriveMovieInfo(movieId)
                .flatMap(movieInfo -> {
                    var reviewListMono = reviewRestClient.retriveReviews(movieId).collectList();
                    return reviewListMono.map(reviews -> new Movie(movieInfo, reviews));
                });

    }

}
