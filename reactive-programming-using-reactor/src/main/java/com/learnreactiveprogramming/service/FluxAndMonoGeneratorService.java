package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    Flux<String> forNames(){
        return Flux.fromIterable(List.of("Marlan", "Joe", "Fernando")).log();
    }

    Flux<String> forNamesMap() {
        return Flux.fromIterable(List.of("Marlan", "Joe", "Fernando")).map(String::toUpperCase).log();
    }

    Flux<String> forNamesFlatMap(int length) {
        return Flux.fromIterable(List.of("Marlan", "Joe", "Fernando"))
                .map(String::toUpperCase)
                .filter(s -> s.length()>length)
                .flatMap(this::splitString)
                .log();
    }

    Flux<String> forNamesFlatMapAsync(int length) {
        return Flux.fromIterable(List.of("Marlan", "Joe", "Fernando"))
                .map(String::toUpperCase)
                .filter(s -> s.length()>length)
                .flatMap(this::splitStringDelay)
                .log();
    }

    Flux<String> forNamesConcatMap(int length) {
        return Flux.fromIterable(List.of("Marlan", "Joe", "Fernando"))
                .map(String::toUpperCase)
                .filter(s -> s.length()>length)
                .concatMap(this::splitStringDelay)
                .log();
    }

    Flux<String> splitString(String name) {
        var charArray = name.split("");
        return Flux.fromArray(charArray);
    }

    Flux<String> splitStringDelay(String name) {
        var charArray = name.split("");
        return Flux.fromArray(charArray).delayElements(Duration.ofMillis(new Random().nextInt(1000)));
    }

    Flux<String> forNamesFilter(int length) {
        return Flux.fromIterable(List.of("Marlan", "Joe", "Fernando"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > length)
                .map(s-> s.length() + "-" + s)
                .log();

    }


    Mono<String> forName() {
        return Mono.just("Marlan Fernando").log();
    }

    Mono<List<String>> forNameFlatMap(int length) {
        return Mono.just("Marlan Fernando")
                .map(String::toUpperCase)
                .filter(s -> s.length() > length)
                .flatMap(this::toFlatMap);
    }

    /**
     * If the Mono pipeline returns a flux then can use flatMapMany operator
     * @param length
     * @return
     */
    Flux<String> forNameFlatMapMany(int length) {
        return Mono.just("Marlan Fernando")
                .map(String::toUpperCase)
                .filter(s -> s.length() > length)
                .flatMapMany(this::splitString);
    }

    Flux<String> forNamesTransform(int length) {

        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length()>length);

        return Flux.fromIterable(List.of("Marlan", "Joe", "Fernando"))
                .transform(filterMap)
                .flatMap(this::splitString)
                .log();
    }

    /**
     * Returns default value
     * @param length
     * @return
     */
    Flux<String> forNamesDefaultIfEmpty(int length) {

        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length()>length);

        return Flux.fromIterable(List.of("Marlan", "Joe", "Fernando"))
                .transform(filterMap)
                .flatMap(this::splitString)
                .defaultIfEmpty("default")
                .log();
    }

    /**
     * returns defalut flux
     * @param length
     * @return
     */
    Flux<String> forNamesSwitchIfEmpty(int length) {

        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toLowerCase)
                .filter(s -> s.length()>length)
                .flatMap(this::splitString);

        Flux<String> defaultFlux = Flux.just("DEFAULT").transform(filterMap);

        return Flux.fromIterable(List.of("Marlan", "Joe", "Fdo"))
                .transform(filterMap)
                .switchIfEmpty(defaultFlux)
                .log();
    }

    private Mono<List<String>> toFlatMap(String s) {

        var charArray = s.split("");
        var charList = List.of(charArray);

        return Mono.just(charList);
    }

    /*
    Read about concat, concatWith, merge, mergeWith as well, might need this
     */

    Flux<String> mergeSeq() {

        var abcFlux = Flux.just("A","B","C").delayElements(Duration.ofMillis(100));
        var cdeFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));

        return Flux.mergeSequential(abcFlux, cdeFlux);

    }

    Flux<String> exploreZip() {

        var abcFlux = Flux.just("A","B","C");
        var cdeFlux = Flux.just("D", "E", "F");

        return Flux.zip(abcFlux, cdeFlux, (first,second) -> first+second);

    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

        fluxAndMonoGeneratorService.forNames().subscribe(name -> {
            System.out.println("Name is :" + name);
        });

        fluxAndMonoGeneratorService.forName().subscribe(name -> {
            System.out.println("Mono name is : " + name);
        });
    }
}
