package edu.cit.AssessMate.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.AssessMate.Model.Question;
import edu.cit.AssessMate.Model.QuestionOption;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    List<QuestionOption> findByQuestionAndIsCorrectTrue(Question question);
}
