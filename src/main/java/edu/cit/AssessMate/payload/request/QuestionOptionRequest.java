package edu.cit.AssessMate.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionOptionRequest {
    @NotBlank
    private String optionText;
    
    private Boolean isCorrect = false;
}
