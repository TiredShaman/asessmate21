package edu.cit.AssessMate.Repository;

import edu.cit.AssessMate.Model.Quiz;
import edu.cit.AssessMate.Model.QuizSubmission;
import edu.cit.AssessMate.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    List<QuizSubmission> findByStudent(User student);
    List<QuizSubmission> findByQuiz(Quiz quiz);
    Optional<QuizSubmission> findByQuizAndStudent(Quiz quiz, User student);
}