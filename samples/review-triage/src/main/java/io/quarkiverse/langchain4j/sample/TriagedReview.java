package io.quarkiverse.langchain4j.sample;

import com.fasterxml.jackson.annotation.JsonCreator;

public record TriagedReview(Evaluation evaluation, String message) {

    @JsonCreator
    public TriagedReview(Evaluation evaluation, String message) {
        this.evaluation = evaluation;
        this.message = message;
    }

}
