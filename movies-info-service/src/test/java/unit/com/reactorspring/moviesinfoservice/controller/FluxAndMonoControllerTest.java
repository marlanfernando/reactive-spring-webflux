package com.reactorspring.moviesinfoservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void flux() {

        webTestClient.get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Long.class)
                .hasSize(3);
    }

    @Test
    void fluxApproach2() {
        var flux = webTestClient.get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(1L,2L,3L)
                .verifyComplete();
    }

    @Test
    void fluxApproach3() {
        webTestClient.get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Long.class)
                .consumeWith(consumer -> {
                   var list = consumer.getResponseBody();
                   assertNotNull(list);
                   assertEquals(3, list.size());
                });

    }

    @Test
    void monoTest() {
        webTestClient.get()
                .uri("/mono")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(consumer -> {
                    var stringToCheck = consumer.getResponseBody();
                    assertNotNull(stringToCheck);
                    assertEquals("Hello world", stringToCheck);
                });

    }

    @Test
    void stream() {
        var flux = webTestClient.get()
                .uri("/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Long.class)
                .getResponseBody();

        /**
         * Note to self : this way we can verify a stream of events, when the test method calls the thenCancel method
         * it will stop calling the stream and then start verify the response received
         */
        StepVerifier.create(flux)
                .expectNext(0L,1L,2L,3L)
                .thenCancel()
                .verify();
    }
}