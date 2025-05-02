package edu.cit.AssessMate.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    private String code;
    
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;
    
    @ManyToMany
    @JoinTable(
        name = "course_students",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> enrolledStudents = new HashSet<>();
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Quiz> quizzes = new HashSet<>();
    
    public Set<Quiz> getQuizzes() {
        return new HashSet<>(quizzes);
    }
}
