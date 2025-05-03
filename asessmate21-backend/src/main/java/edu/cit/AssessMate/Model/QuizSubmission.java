package edu.cit.AssessMate.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz_submissions")
public class QuizSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
    
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;
    
    private LocalDateTime submissionTime;
    
    private Double score;
    
    private Boolean isGraded = false;
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL)
    private List<Answer> answers = new ArrayList<>();
}