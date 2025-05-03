package edu.cit.AssessMate.payload.response;

import edu.cit.AssessMate.Model.Quiz;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuizDetailResponse {
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private Boolean isActive;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private List<QuestionDTO> questions;

    public static QuizDetailResponse fromEntity(Quiz quiz) {
        QuizDetailResponse dto = new QuizDetailResponse();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setCourseId(quiz.getCourse() != null ? quiz.getCourse().getId() : null);
        dto.setIsActive(quiz.getIsActive());
        dto.setStartTime(quiz.getStartTime());
        dto.setEndTime(quiz.getEndTime());
 	    dto.setDurationMinutes(quiz.getDurationMinutes());
        //dto.setQuestions(new ArrayList<>(quiz.getQuestions()));
        return dto;
    }
}
