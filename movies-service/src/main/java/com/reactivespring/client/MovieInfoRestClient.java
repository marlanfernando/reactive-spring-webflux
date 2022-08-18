package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

@Component
public class MovieInfoRestClient {

    WebClient webClient;

    @Value("${restClient.moviesInfo}")
    private String moviesInfoUrl;

    public MovieInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retriveMovieInfo(String movieId) {

        var url = moviesInfoUrl.concat("/{id}");

        var retrySpec = Retry.fixedDelay(3, Duration.ofMillis(100))
                .filter(ex -> ex instanceof MoviesInfoServerException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()));


        return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(
                                new MoviesInfoClientException("No Movie found for given movieId : " + movieId,
                                        clientResponse.statusCode().value() ));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(value ->
                                    Mono.error(new MoviesInfoClientException(value, clientResponse.statusCode().value())));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                            .flatMap(value ->
                                    Mono.error(new MoviesInfoServerException(value)))
                )
                .bodyToMono(MovieInfo.class)
                .retryWhen(retrySpec)
                .log();
    }
}
