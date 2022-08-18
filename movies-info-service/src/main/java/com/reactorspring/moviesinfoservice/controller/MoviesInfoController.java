package com.reactorspring.moviesinfoservice.controller;

import com.reactorspring.moviesinfoservice.domain.MovieInfo;
import com.reactorspring.moviesinfoservice.service.MovieInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class MoviesInfoController {

    MovieInfoService movieInfoService;

    public MoviesInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().all();

    @GetMapping("/movieInfos")
    public Flux<MovieInfo> getAllMovies(@RequestParam(value="year", required = false) Integer year ) {
        if(year != null)
            return movieInfoService.findByYear(year);

        return movieInfoService.getAllMovies();
    }

    @GetMapping("/movieInfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getAllMovies(@PathVariable String id) {
        return movieInfoService.getMovieInfoById(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping(value = "/movieInfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> getMoviesStream() {
        return movieInfoSink.asFlux();
    }

    @PostMapping("/movieInfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {

        return movieInfoService.save(movieInfo).doOnNext(movieInfoSink::tryEmitNext);
    }

    @PutMapping("/movieInfos/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<MovieInfo>> updateMovie(@RequestBody @Valid MovieInfo movieInfo, @PathVariable String id) {

        return movieInfoService.updateMovie(movieInfo,id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/movieInfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovie(@PathVariable String id) {
        return movieInfoService.deleteMovie(id);
    }

}
