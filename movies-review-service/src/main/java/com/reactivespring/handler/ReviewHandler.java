package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {


    @Autowired
    Validator validator;

    ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    Sinks.Many<Review> reviewSinks = Sinks.many().replay().all();

    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .doOnNext(reviewSinks::tryEmitNext)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);

    }

    private void validate(Review review) {

        var constraintViolations = validator.validate(review);

        if(!constraintViolations.isEmpty()) {
            log.error("Constraint Violations : {}", constraintViolations);

            var errorMessage = constraintViolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));

            throw new ReviewDataException(errorMessage);

        }

    }

    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {

        var movieInfoId = serverRequest.queryParam("movieInfoId");

        if(movieInfoId.isPresent()) {
            var reviews = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get())).log() ;
            return ServerResponse.ok().body(reviews, Review.class);
        }

        var reviews = reviewReactiveRepository.findAll();
        return ServerResponse.ok().body(reviews, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {

        var reviewId = serverRequest.pathVariable("id");

        return reviewReactiveRepository.findById(reviewId)
                // If the id does not have value in the db then return an exception here
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for given id : "+ reviewId)))
                // this mapping is done to the object that returns from the repository
                .flatMap(review -> serverRequest.bodyToMono(Review.class)
                        // and then get the value from the request and map the request value to the value returns from
                        // the reactive DB
                        .map(reqReview -> {
                            // this is the mapping that converts the value in DB to value from the request
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        // this transformation is done to the updated 'review' object that returns from the above map
                        .flatMap(reviewReactiveRepository::save)
                        // the above flatmap returns the updated object after the save operation is completed and then
                        // returns the server response
                        .flatMap(ServerResponse.status(HttpStatus.OK)::bodyValue))
                /*
                one other way to respond back with 404 is below way
                .switchIfEmpty(ServerResponse.notFound().build()
                 */

                .log();

    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {
        var reviewId = serverRequest.pathVariable("id");

        var existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview.flatMap(review -> reviewReactiveRepository.delete(review))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> streamReview(ServerRequest serverRequest) {

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewSinks.asFlux(), Review.class);
    }
}
