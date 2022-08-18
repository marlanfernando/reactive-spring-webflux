package com.reactorspring.moviesinfoservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class MovieInfo {

    @Id
    private String movieInfoId;
    @NotBlank(message = "movieInfo.name should not be blank")
    private String name;
    @NotNull
    @Positive(message = "movieInfo.year should be a possitive integer")
    private Integer year;

    private List<@NotBlank(message = "movieInfo.cast should be present") String> casts;
    private LocalDate releaseDate;
}
