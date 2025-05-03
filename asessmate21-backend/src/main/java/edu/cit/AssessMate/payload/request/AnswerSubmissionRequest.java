package edu.cit.AssessMate.payload.request;

import lombok.Data;

import java.util.List;

@Data
public class AnswerSubmissionRequest {
    private String textAnswer;
    private List<Long> selectedOptionIds;
}