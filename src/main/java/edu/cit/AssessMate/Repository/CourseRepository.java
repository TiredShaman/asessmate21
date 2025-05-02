package edu.cit.AssessMate.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.cit.AssessMate.Model.Course;
import edu.cit.AssessMate.Model.User;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Method to count courses by the teacher's ID
    long countByTeacherId(Long teacherId);

    // Method to count distinct students enrolled in courses taught by a specific teacher
    @Query("SELECT COUNT(DISTINCT s) FROM Course c JOIN c.enrolledStudents s WHERE c.teacher = :teacher")
    long countDistinctStudentsByTeacher(@Param("teacher") User teacher);

    // Fetch courses with teacher and enrolled students eagerly
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher LEFT JOIN FETCH c.enrolledStudents WHERE c.teacher = :teacher")
    List<Course> findByTeacherWithDetails(@Param("teacher") User teacher);

    // Find courses by teacher (basic fetch without eager loading)
    List<Course> findByTeacher(User teacher);

    // Find courses containing a specific student
    @Query("SELECT c FROM Course c JOIN FETCH c.enrolledStudents s WHERE s = :student")
    List<Course> findByEnrolledStudentsContaining(@Param("student") User student);

    // Find a course by ID with enrolled students eagerly
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.enrolledStudents WHERE c.id = :id")
    Optional<Course> findByIdWithEnrolledStudents(@Param("id") Long id);

    
}