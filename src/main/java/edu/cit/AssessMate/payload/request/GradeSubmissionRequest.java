package edu.cit.AssessMate.payload.request;

import lombok.Data;

import java.util.Map;

@Data
public class GradeSubmissionRequest {
    private Double score;
    private Map<String, AnswerFeedback> answerFeedback;
}