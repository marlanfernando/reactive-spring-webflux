package com.reactorspring.moviesinfoservice.service;

import com.reactorspring.moviesinfoservice.domain.MovieInfo;
import com.reactorspring.moviesinfoservice.repository.MovieInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovieInfoService {

    MovieInfoRepository movieInfoRepository;

    @Autowired
    public void setMovieInfoRepository(MovieInfoRepository movieInfoRepository) {
        this.movieInfoRepository = movieInfoRepository;
    }

    public Mono<MovieInfo> save(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo);
    }

    public Flux<MovieInfo> getAllMovies() {
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> getMovieInfoById(String id) {
        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo> updateMovie(MovieInfo updateMovieInfo, String id) {

        return  movieInfoRepository.findById(id)
                .flatMap(movieInfo -> {
                     movieInfo.setName(updateMovieInfo.getName());
                     movieInfo.setYear(updateMovieInfo.getYear());
                     movieInfo.setCasts(updateMovieInfo.getCasts());
                     movieInfo.setReleaseDate(updateMovieInfo.getReleaseDate());
                     return movieInfoRepository.save(movieInfo);
                });

    }

    public Mono<Void> deleteMovie(String id) {
        return movieInfoRepository.deleteById(id);
    }

    public Flux<MovieInfo> findByYear(Integer year) {
        return movieInfoRepository.findByYear(year);
    }
}
