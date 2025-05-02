package edu.cit.AssessMate.payload.request;

import lombok.Data;

import java.util.Map;

@Data
public class QuizSubmissionRequest {
    private Map<Long, AnswerSubmissionRequest> answers;
}
