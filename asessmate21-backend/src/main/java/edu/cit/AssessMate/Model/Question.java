package edu.cit.AssessMate.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String questionText;
    
    private QuestionType type;
    
    private Double points = 1.0;
    
    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<QuestionOption> options = new ArrayList<>();
    
    public enum QuestionType {
        MULTIPLE_CHOICE,
        SINGLE_CHOICE,
        TRUE_FALSE,
        SHORT_ANSWER,
        ESSAY
    }
}