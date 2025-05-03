package edu.cit.AssessMate.payload.response;

import lombok.Data;

@Data
public class QuizCreationResponse {
    private String message;
    private Long quizId;

    public QuizCreationResponse(String message, Long quizId) {
        this.message = message;
        this.quizId = quizId;
    }
}
