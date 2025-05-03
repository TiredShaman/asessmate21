package edu.cit.AssessMate.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EnrollStudentRequest {
    @NotBlank
    private String username;
}
