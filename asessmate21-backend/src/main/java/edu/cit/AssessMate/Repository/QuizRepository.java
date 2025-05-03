package edu.cit.AssessMate.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.AssessMate.Model.Course;
import edu.cit.AssessMate.Model.Quiz;
import edu.cit.AssessMate.Model.User;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCourse(Course course);
    List<Quiz> findByCourseAndIsActiveTrue(Course course);
    List<Quiz> findByCourse_Teacher(User teacher);
}
