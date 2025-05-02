package edu.cit.AssessMate.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.cit.AssessMate.Model.QuizQuestion;

/**
 * Repository interface for QuizQuestion entity.
 */
@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    // Define custom query methods if needed
}
