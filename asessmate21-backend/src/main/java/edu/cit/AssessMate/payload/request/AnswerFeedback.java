package edu.cit.AssessMate.payload.request;

import lombok.Data;

@Data
public class AnswerFeedback {
    private Double points;
    private String feedback;
}