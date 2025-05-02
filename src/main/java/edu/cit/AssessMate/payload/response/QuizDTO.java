package edu.cit.AssessMate.payload.response;

import edu.cit.AssessMate.Model.Quiz;
import lombok.Data;

@Data
public class QuizDTO {
    private Long id;
    private String title;
    private String description;
    private Boolean isActive;
    private CourseDTO course;
    private Integer durationMinutes;
    private Long numberOfEnrolledStudents;

    public QuizDTO(Quiz quiz, Long numberOfEnrolledStudents) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.description = quiz.getDescription();
        this.isActive = quiz.getIsActive();
        this.course = (quiz.getCourse() != null) ? CourseDTO.fromEntity(quiz.getCourse()) : null;
        this.durationMinutes = quiz.getDurationMinutes();
        this.numberOfEnrolledStudents = numberOfEnrolledStudents;
    }

    public QuizDTO(Quiz quiz) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.description = quiz.getDescription();
        this.isActive = quiz.getIsActive();
        this.course = (quiz.getCourse() != null) ? CourseDTO.fromEntity(quiz.getCourse()) : null;
        this.durationMinutes = quiz.getDurationMinutes();
        this.numberOfEnrolledStudents = 0L;
    }
}