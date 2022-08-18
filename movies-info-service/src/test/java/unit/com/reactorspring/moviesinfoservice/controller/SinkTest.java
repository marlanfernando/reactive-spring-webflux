package com.reactorspring.moviesinfoservice.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SinkTest {

    @Test
    public void sinkTest() {

        Sinks.Many<Integer> sink = Sinks.many().replay().all();

        sink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        sink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> intFlux = sink.asFlux();
        intFlux.subscribe(integer -> {
            System.out.println(integer);
        });

        assertTrue(true);
    }

    /**
     * Multicast will publish events after a subscriber subscribe to it, previous events will be not visible to new subscriber
     */
    @Test
    public void sinkTest_multicast() {

        Sinks.Many<Integer> multicast = Sinks.many().multicast().onBackpressureBuffer();

        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> intFlux = multicast.asFlux();
        intFlux.subscribe(integer -> {
            System.out.println("1 " + integer);
        });

        Flux<Integer> intFlux2 = multicast.asFlux();
        intFlux2.subscribe(integer -> {
            System.out.println("2 "+ integer);
        });

        multicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);


        assertTrue(true);
    }
}
