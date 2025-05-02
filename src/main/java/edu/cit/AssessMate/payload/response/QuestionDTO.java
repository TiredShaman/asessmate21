package edu.cit.AssessMate.payload.response;

import edu.cit.AssessMate.Model.Question;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class QuestionDTO {
    private Long id;
    private String questionText;
    private Question.QuestionType type;
    private Double points;
    private List<QuestionOptionDTO> options;

    public static QuestionDTO fromEntity(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setType(question.getType());
        dto.setPoints(question.getPoints());
        dto.setOptions(question.getOptions().stream()
                .map(QuestionOptionDTO::fromEntity)
                .collect(Collectors.toList()));
        return dto;
    }
}
