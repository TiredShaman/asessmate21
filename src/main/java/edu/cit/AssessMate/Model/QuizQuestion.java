package edu.cit.AssessMate.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;
import edu.cit.AssessMate.payload.request.QuestionOptionRequest;

@Entity
@Data
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String question;
    private String answer;
    private Double points;
    
    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "question_details_id")
    private Question questionDetails;

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public void setOptions(List<QuestionOptionRequest> optionRequests) {
        if (this.questionDetails == null) {
            this.questionDetails = new Question();
            this.questionDetails.setQuestionText(this.question);
        }

        this.questionDetails.setOptions(optionRequests.stream()
            .map(request -> {
                QuestionOption option = new QuestionOption();
                option.setOptionText(request.getOptionText());
                option.setIsCorrect(request.getIsCorrect());
                option.setQuestion(this.questionDetails);
                return option;
            })
            .collect(Collectors.toList()));
    }

    public void setPoints(Double points) {
        this.points = points;
        if (this.questionDetails != null) {
            this.questionDetails.setPoints(points);
        }
    }
}