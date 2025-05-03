package edu.cit.AssessMate.payload.response;

import edu.cit.AssessMate.Model.QuestionOption;
import lombok.Data;

@Data
public class QuestionOptionDTO {
    private Long id;
    private String optionText;
    private Boolean isCorrect;

    public static QuestionOptionDTO fromEntity(QuestionOption questionOption) {
        QuestionOptionDTO dto = new QuestionOptionDTO();
        dto.setId(questionOption.getId());
        dto.setOptionText(questionOption.getOptionText());
        dto.setIsCorrect(questionOption.getIsCorrect());
        return dto;
    }
}
