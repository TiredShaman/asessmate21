package edu.cit.AssessMate.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuizRequest {
    @NotBlank
    private String title;
    
    private String description;
    
    @NotNull
    private LocalDateTime startTime;
    
    @NotNull
    private LocalDateTime endTime;
    
    @NotNull
    private Integer durationMinutes;
}
