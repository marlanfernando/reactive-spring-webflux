package com.reactorspring.moviesinfoservice.repository;

import com.reactorspring.moviesinfoservice.domain.MovieInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var list = List.of(new MovieInfo(null, "Batman bigins", 2008, List.of("Christian Bale", "Michael cane"), LocalDate.parse("2005-04-23")),
                new MovieInfo(null, "Ironman", 2008, List.of("Rober Downey", "Michael worne"), LocalDate.parse("2008-11-23")),
                new MovieInfo("smnwh", "Spider-man no way home", 2022, List.of("Tom Holland", "Bennadict cumberbatch"), LocalDate.parse("2022-04-23")));

        movieInfoRepository.saveAll(list)
                .blockLast();
    }

    @Test
    void findAllTest() {

        var moviInfos = movieInfoRepository.findAll().log();

        StepVerifier.create(moviInfos)
                .expectNextCount(3)
                .verifyComplete();

    }

    @Test
    void findByTest() {
        var movieInfo = movieInfoRepository.findById("smnwh").log();

        StepVerifier.create(movieInfo)
                .assertNext(movie -> {
                    assertEquals("Spider-man no way home", movie.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveTest() {
        var movieInfo = movieInfoRepository.save(new MovieInfo(null, "Ironman 2", 2012, List.of("Rober Downey", "Michael worne"), LocalDate.parse("2012-11-23"))).log();

        StepVerifier.create(movieInfo)
                .assertNext(movie -> {
                    assertNotNull(movie.getMovieInfoId());
                    assertEquals("Ironman 2", movie.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateTest() {
        var movie = movieInfoRepository.findById("smnwh").block();
        movie.setYear(2021);

        var movieInfo = movieInfoRepository.save(movie);

        StepVerifier.create(movieInfo)
                .assertNext(movieInfo1 -> {
                    assertEquals(2021, movieInfo1.getYear());
                });
    }

    @Test
    void deleteTest() {

        movieInfoRepository.deleteById("smnwh").block();

        var movieInfos = movieInfoRepository.findAll().log();

        StepVerifier.create(movieInfos)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByYearTest() {
        var _2008Movies = movieInfoRepository.findByYear(2008);

        StepVerifier.create(_2008Movies)
                .expectNextCount(2)
                .verifyComplete();
    }


    @Test
    void findByName() {
        var movieByName = movieInfoRepository.findByName("Ironman");

        StepVerifier.create(movieByName)
                .expectNextCount(1)
                .assertNext(movieInfo -> {
                   assert movieInfo != null;
                   assertEquals("Ironman", movieInfo.getName());
                });
    }
}