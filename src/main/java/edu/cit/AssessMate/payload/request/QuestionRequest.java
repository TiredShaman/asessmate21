package edu.cit.AssessMate.payload.request;

import edu.cit.AssessMate.Model.Question.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    @NotBlank
    private String questionText;
    
    @NotNull
    private QuestionType type;
    
    private Double points = 1.0;

    public Double getPoints() {
        return points;
    }
    
    private List<QuestionOptionRequest> options;
}
