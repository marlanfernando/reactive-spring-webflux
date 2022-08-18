package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    public void testFlux() {

        var values = fluxAndMonoGeneratorService.forNames();

        StepVerifier.create(values)
                .expectNext("Marlan", "Joe", "Fernando")
                .verifyComplete();
    }

    @Test
    void forNamesMap() {

        var values = fluxAndMonoGeneratorService.forNamesMap();

        StepVerifier.create(values)
                .expectNext("MARLAN", "JOE", "FERNANDO")
                .verifyComplete();

    }

    @Test
    void forNamesFilter() {

        var values = fluxAndMonoGeneratorService.forNamesFilter(3);

        StepVerifier.create(values)
                .expectNext("6-MARLAN", "8-FERNANDO")
                .verifyComplete();

    }

    @Test
    void forNamesFlatMap() {
        var values = fluxAndMonoGeneratorService.forNamesFlatMap(3);

        StepVerifier.create(values)
                .expectNext("M","A","R","L","A","N", "F","E","R","N","A","N","D","O")
                .verifyComplete();
    }

    @Test
    void forNamesFlatMapAsync() {
        var values = fluxAndMonoGeneratorService.forNamesFlatMapAsync(3);

        StepVerifier.create(values)
                .expectNextCount(14)
                .verifyComplete();

    }

    @Test
    void forNamesConcatMap() {
        var values = fluxAndMonoGeneratorService.forNamesFlatMap(3);

        StepVerifier.create(values)
                .expectNext("M","A","R","L","A","N", "F","E","R","N","A","N","D","O")
                .verifyComplete();

    }

    @Test
    void forNameFlatMap() {

        var values = fluxAndMonoGeneratorService.forNameFlatMap(3);

        StepVerifier.create(values)
                .expectNext(List.of("M","A","R","L","A","N", " ", "F","E","R","N","A","N","D","O"))
                .verifyComplete();
    }

    @Test
    void forNameFlatMapMany() {

        var values = fluxAndMonoGeneratorService.forNameFlatMapMany(3);

        StepVerifier.create(values)
                .expectNext("M","A","R","L","A","N", " ", "F","E","R","N","A","N","D","O")
                .verifyComplete();
    }

    @Test
    void forNamesTransform() {

        var values = fluxAndMonoGeneratorService.forNamesFlatMap(3);

        StepVerifier.create(values)
                .expectNext("M","A","R","L","A","N", "F","E","R","N","A","N","D","O")
                .verifyComplete();
    }

    @Test
    void forNamesTransformDefaultIfEmpty() {

        var values = fluxAndMonoGeneratorService.forNamesDefaultIfEmpty(8);

        StepVerifier.create(values)
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    void forNamesTransformSwitchIfEmpty() {

        var values = fluxAndMonoGeneratorService.forNamesSwitchIfEmpty(6);

        StepVerifier.create(values)
                .expectNext("d","e","f","a","u","l","t")
                .verifyComplete();
    }

    @Test
    void mergeSeq() {

        var values = fluxAndMonoGeneratorService.mergeSeq();

        StepVerifier.create(values)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    void exploreZip() {
        var values = fluxAndMonoGeneratorService.exploreZip();

        StepVerifier.create(values)
                .expectNext("AD","BE","CF")
                .verifyComplete();
    }
}