package edu.cit.AssessMate.Repository;

import edu.cit.AssessMate.Model.Question;
import edu.cit.AssessMate.Model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuiz(Quiz quiz);
}