package edu.cit.AssessMate.Controller;

import edu.cit.AssessMate.Model.*;
import edu.cit.AssessMate.payload.response.QuestionDTO;
import edu.cit.AssessMate.payload.request.AnswerSubmissionRequest;
import edu.cit.AssessMate.payload.request.QuizSubmissionRequest;
import edu.cit.AssessMate.payload.response.MessageResponse;
import edu.cit.AssessMate.payload.response.QuizDetailResponse;
import edu.cit.AssessMate.Repository.*;
import edu.cit.AssessMate.Service.impl.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    @GetMapping("/courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getEnrolledCourses() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User student = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Student not found"));

            // Get the courses from repository
            List<Course> fetchedCourses = courseRepository.findByEnrolledStudentsContaining(student);
            
            // Convert each course to a simple Map to avoid serialization issues
            List<Map<String, Object>> simplifiedCourses = new ArrayList<>();
            for (Course course : fetchedCourses) {
                Map<String, Object> simplifiedCourse = new HashMap<>();
                simplifiedCourse.put("id", course.getId());
                
                // Use reflection to safely get properties that might not exist
                try {
                    // Try to get course name using different possible getter methods
                    try {
                        java.lang.reflect.Method getNameMethod = course.getClass().getMethod("getName");
                        simplifiedCourse.put("name", getNameMethod.invoke(course));
                    } catch (NoSuchMethodException e) {
                        try {
                            java.lang.reflect.Method getTitleMethod = course.getClass().getMethod("getTitle");
                            simplifiedCourse.put("name", getTitleMethod.invoke(course));
                        } catch (NoSuchMethodException e2) {
                            simplifiedCourse.put("name", "Course " + course.getId());
                        }
                    }
                    
                    // Try to get description
                    try {
                        java.lang.reflect.Method getDescMethod = course.getClass().getMethod("getDescription");
                        simplifiedCourse.put("description", getDescMethod.invoke(course));
                    } catch (NoSuchMethodException e) {
                        simplifiedCourse.put("description", "");
                    }
                    
                    // Try to extract instructor details if available
                    try {
                        java.lang.reflect.Method getInstructorMethod = course.getClass().getMethod("getInstructor");
                        Object instructor = getInstructorMethod.invoke(course);
                        if (instructor != null) {
                            Map<String, Object> instructorMap = new HashMap<>();
                            instructorMap.put("id", instructor.getClass().getMethod("getId").invoke(instructor));
                            
                            try {
                                instructorMap.put("username", instructor.getClass().getMethod("getUsername").invoke(instructor));
                            } catch (Exception e) {
                                instructorMap.put("username", "");
                            }
                            
                            simplifiedCourse.put("instructor", instructorMap);
                        }
                    } catch (Exception e) {
                        // Instructor details not available
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                simplifiedCourses.add(simplifiedCourse);
            }
            
            return ResponseEntity.ok(simplifiedCourses);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            return ResponseEntity.status(500).body(new MessageResponse("An error occurred while retrieving courses: " + e.getMessage()));
        }
    }
    
    @GetMapping("/courses/{courseId}/quizzes")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getCourseQuizzes(@PathVariable Long courseId) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User student = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Student not found"));
            
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
                    
            // Check if student is enrolled in the course
            if (!course.getEnrolledStudents().contains(student)) {
                return ResponseEntity.badRequest().body(new MessageResponse("You are not enrolled in this course"));
            }
            
            List<Quiz> quizzes = quizRepository.findByCourseAndIsActiveTrue(course);
            
            // Filter quizzes to only include those the student is enrolled in
            List<Quiz> enrolledQuizzes = quizzes.stream()
                    .filter(quiz -> quiz.getCourse().getEnrolledStudents().contains(student))
                    .collect(Collectors.toList());
            
            // Convert to simplified objects to avoid circular references
            List<Map<String, Object>> simplifiedQuizzes = new ArrayList<>();
            for (Quiz quiz : enrolledQuizzes) {
                Map<String, Object> simplifiedQuiz = new HashMap<>();
                simplifiedQuiz.put("id", quiz.getId());
                
                // Extract other quiz fields safely
                try {
                    java.lang.reflect.Method getTitleMethod = quiz.getClass().getMethod("getTitle");
                    simplifiedQuiz.put("title", getTitleMethod.invoke(quiz));
                } catch (Exception e) {
                    simplifiedQuiz.put("title", "Quiz " + quiz.getId());
                }
                
                try {
                    java.lang.reflect.Method getDescMethod = quiz.getClass().getMethod("getDescription");
                    simplifiedQuiz.put("description", getDescMethod.invoke(quiz));
                } catch (Exception e) {
                    simplifiedQuiz.put("description", "");
                }
                
                try {
                    java.lang.reflect.Method getIsActiveMethod = quiz.getClass().getMethod("getIsActive");
                    simplifiedQuiz.put("isActive", getIsActiveMethod.invoke(quiz));
                } catch (Exception e) {
                    simplifiedQuiz.put("isActive", true); // Assume active since it was returned from findByCourseAndIsActiveTrue
                }
                
                simplifiedQuizzes.add(simplifiedQuiz);
            }
            
            return ResponseEntity.ok(simplifiedQuizzes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new MessageResponse("An error occurred while retrieving quizzes: " + e.getMessage()));
        }
    }
    
    @GetMapping("/quizzes/{quizId}")
@PreAuthorize("hasRole('STUDENT')")
public ResponseEntity<?> getQuizDetails(@PathVariable Long quizId) {
    try {
        logger.info("Fetching quiz details for quizId: {}", quizId);
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User student = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> {
                    logger.error("Student not found with ID: {}", userDetails.getId());
                    return new RuntimeException("Student not found");
                });

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    logger.error("Quiz not found with ID: {}", quizId);
                    return new RuntimeException("Quiz not found");
                });

        Course course = courseRepository.findByIdWithEnrolledStudents(quiz.getCourse().getId())
                .orElseThrow(() -> {
                    logger.error("Course not found with ID: {}", quiz.getCourse().getId());
                    return new RuntimeException("Course not found");
                });

        // Check if student is enrolled in the course
        if (!course.getEnrolledStudents().contains(student)) {
            logger.warn("Student {} not enrolled in course {}", student.getId(), course.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("You are not enrolled in course: " + course.getTitle()));
        }

        // Check if quiz is active
        if (!quiz.getIsActive()) {
            logger.warn("Quiz {} is not active", quizId);
            return ResponseEntity.badRequest().body(new MessageResponse("This quiz is not active"));
        }

        // Check if student has already submitted this quiz
        Optional<QuizSubmission> existingSubmission = quizSubmissionRepository.findByQuizAndStudent(quiz, student);
        if (existingSubmission.isPresent()) {
            logger.warn("Student {} already submitted quiz {}", student.getId(), quizId);
            return ResponseEntity.badRequest().body(new MessageResponse("You have already submitted this quiz"));
        }

        // Fetch questions
        List<Question> questions = questionRepository.findByQuiz(quiz);
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(QuestionDTO::fromEntity)
                .collect(Collectors.toList());

        QuizDetailResponse response = QuizDetailResponse.fromEntity(quiz);
        response.setQuestions(questionDTOs);

        logger.info("Successfully fetched quiz details for quizId: {}", quizId);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        logger.error("Error fetching quiz details for quizId: {}", quizId, e);
        return ResponseEntity.status(500).body(new MessageResponse("An error occurred while retrieving quiz details: " + e.getMessage()));
    }
}
@PostMapping("/quizzes/{quizId}/submit")
@PreAuthorize("hasRole('STUDENT')")
public ResponseEntity<?> submitQuiz(@PathVariable Long quizId, @Valid @RequestBody QuizSubmissionRequest submissionRequest) {
    try {
        logger.info("Processing quiz submission for quizId: {}", quizId);
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User student = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> {
                    logger.error("Student not found with ID: {}", userDetails.getId());
                    return new RuntimeException("Student not found");
                });

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    logger.error("Quiz not found with ID: {}", quizId);
                    return new RuntimeException("Quiz not found");
                });

        Course course = courseRepository.findByIdWithEnrolledStudents(quiz.getCourse().getId())
                .orElseThrow(() -> {
                    logger.error("Course not found with ID: {}", quiz.getCourse().getId());
                    return new RuntimeException("Course not found");
                });

        if (!course.getEnrolledStudents().contains(student)) {
            logger.warn("Student {} not enrolled in course {}", student.getId(), course.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("You are not enrolled in this course"));
        }

        if (!quiz.getIsActive()) {
            logger.warn("Quiz {} is not active", quizId);
            return ResponseEntity.badRequest().body(new MessageResponse("This quiz is not active"));
        }

        Optional<QuizSubmission> existingSubmission = quizSubmissionRepository.findByQuizAndStudent(quiz, student);
        if (existingSubmission.isPresent()) {
            logger.warn("Student {} already submitted quiz {}", student.getId(), quizId);
            return ResponseEntity.badRequest().body(new MessageResponse("You have already submitted this quiz"));
        }

        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setStudent(student);
        submission.setSubmissionTime(LocalDateTime.now());
        submission.setIsGraded(false); // Default to ungraded
        QuizSubmission savedSubmission = quizSubmissionRepository.save(submission);

        double totalScore = 0.0;
        boolean allAutoGraded = true;
        List<Answer> answers = new ArrayList<>();

        for (Map.Entry<Long, AnswerSubmissionRequest> entry : submissionRequest.getAnswers().entrySet()) {
            Long questionId = entry.getKey();
            AnswerSubmissionRequest answerRequest = entry.getValue();

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> {
                        logger.error("Question not found: {}", questionId);
                        return new RuntimeException("Question not found: " + questionId);
                    });

            Answer answer = new Answer();
            answer.setQuestion(question);
            answer.setSubmission(savedSubmission);
            answer.setTextAnswer(answerRequest.getTextAnswer());

            double questionScore = 0.0;
            logger.info("Processing question ID: {}, Type: {}", questionId, question.getType());

            switch (question.getType()) {
                case MULTIPLE_CHOICE:
                    if (answerRequest.getSelectedOptionIds() != null && !answerRequest.getSelectedOptionIds().isEmpty()) {
                        List<QuestionOption> selectedOptions = questionOptionRepository.findAllById(answerRequest.getSelectedOptionIds());
                        List<QuestionOption> correctOptions = questionOptionRepository.findByQuestionAndIsCorrectTrue(question);

                        logger.info("Selected Options: {}, Correct Options: {}", selectedOptions.size(), correctOptions.size());

                        if (!correctOptions.isEmpty()) {
                            // Calculate partial credit: score = (correct selections / total correct options) * points
                            long correctSelections = selectedOptions.stream()
                                    .filter(opt -> correctOptions.contains(opt))
                                    .count();
                            questionScore = (correctSelections / (double) correctOptions.size()) * question.getPoints();
                            answer.setSelectedOptions(selectedOptions);
                            logger.info("MULTIPLE_CHOICE Score: {} / {}", questionScore, question.getPoints());
                        }
                    }
                    break;

                case SINGLE_CHOICE:
                case TRUE_FALSE:
                    if (answerRequest.getSelectedOptionIds() != null && !answerRequest.getSelectedOptionIds().isEmpty()) {
                        List<QuestionOption> selectedOptions = questionOptionRepository.findAllById(answerRequest.getSelectedOptionIds());
                        List<QuestionOption> correctOptions = questionOptionRepository.findByQuestionAndIsCorrectTrue(question);

                        logger.info("Selected Options: {}, Correct Options: {}", selectedOptions.size(), correctOptions.size());

                        if (selectedOptions.size() == correctOptions.size() && selectedOptions.containsAll(correctOptions)) {
                            questionScore = question.getPoints();
                            logger.info("{} Score: {} / {}", question.getType(), questionScore, question.getPoints());
                        }
                        answer.setSelectedOptions(selectedOptions);
                    }
                    break;

                case SHORT_ANSWER:
                case ESSAY:
                    // Leave ungraded for teacher review
                    allAutoGraded = false;
                    logger.info("{} question requires manual grading", question.getType());
                    answer.setTextAnswer(answerRequest.getTextAnswer());
                    break;

                default:
                    logger.warn("Unsupported question type: {}", question.getType());
                    allAutoGraded = false;
            }

            answer.setPoints(questionScore);
            totalScore += questionScore;
            answers.add(answer);
        }

        savedSubmission.setAnswers(answers);
        savedSubmission.setScore(totalScore);
        savedSubmission.setIsGraded(allAutoGraded);
        quizSubmissionRepository.save(savedSubmission);

        logger.info("Quiz submission saved with ID: {}, Score: {}, Graded: {}", savedSubmission.getId(), totalScore, allAutoGraded);
        return ResponseEntity.ok(new MessageResponse("Quiz submitted successfully!"));
    } catch (Exception e) {
        logger.error("Error submitting quiz {}: {}", quizId, e.getMessage(), e);
        return ResponseEntity.status(500).body(new MessageResponse("An error occurred while submitting quiz: " + e.getMessage()));
    }
}
    @GetMapping("/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getStudentSubmissions() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User student = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Student not found"));
            
            List<QuizSubmission> submissions = quizSubmissionRepository.findByStudent(student);
            
            // Convert to simplified objects to avoid circular references
            List<Map<String, Object>> simplifiedSubmissions = new ArrayList<>();
            for (QuizSubmission sub : submissions) {
                Map<String, Object> simplifiedSub = new HashMap<>();
                simplifiedSub.put("id", sub.getId());
                simplifiedSub.put("submissionTime", sub.getSubmissionTime());
                simplifiedSub.put("isGraded", sub.getIsGraded());
                simplifiedSub.put("score", sub.getScore());
                
                // Add quiz info
                Map<String, Object> quizInfo = new HashMap<>();
                quizInfo.put("id", sub.getQuiz().getId());
                try {
                    quizInfo.put("title", sub.getQuiz().getClass().getMethod("getTitle").invoke(sub.getQuiz()));
                } catch (Exception e) {
                    quizInfo.put("title", "Quiz " + sub.getQuiz().getId());
                }
                simplifiedSub.put("quiz", quizInfo);
                
                // Add course info if available
                try {
                    Object course = sub.getQuiz().getClass().getMethod("getCourse").invoke(sub.getQuiz());
                    if (course != null) {
                        Map<String, Object> courseInfo = new HashMap<>();
                        courseInfo.put("id", course.getClass().getMethod("getId").invoke(course));
                        try {
                            courseInfo.put("name", course.getClass().getMethod("getName").invoke(course));
                        } catch (Exception e) {
                            courseInfo.put("name", "Course " + course.getClass().getMethod("getId").invoke(course));
                        }
                        simplifiedSub.put("course", courseInfo);
                    }
                } catch (Exception e) {
                    // Course info not available
                }
                
                simplifiedSubmissions.add(simplifiedSub);
            }
            
            return ResponseEntity.ok(simplifiedSubmissions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new MessageResponse("An error occurred while retrieving submissions: " + e.getMessage()));
        }
    }
    @GetMapping("/grades")
@PreAuthorize("hasRole('STUDENT')")
public ResponseEntity<?> getStudentGrades() {
    try {
        logger.info("Fetching grades for student");
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User student = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> {
                    logger.error("Student not found with ID: {}", userDetails.getId());
                    return new RuntimeException("Student not found");
                });

        List<QuizSubmission> submissions = quizSubmissionRepository.findByStudent(student);
        List<Map<String, Object>> grades = submissions.stream()
                .filter(sub -> sub.getIsGraded() != null && sub.getIsGraded())
                .map(sub -> {
                    Map<String, Object> grade = new HashMap<>();
                    grade.put("id", sub.getId());
                    grade.put("quizTitle", sub.getQuiz().getTitle());
                    grade.put("courseName", sub.getQuiz().getCourse().getTitle());
                    grade.put("score", sub.getScore());
                    grade.put("date", sub.getSubmissionTime().toString());
                    // Calculate maxScore from quiz questions
                    double maxScore = questionRepository.findByQuiz(sub.getQuiz())
                            .stream()
                            .mapToDouble(Question::getPoints)
                            .sum();
                    grade.put("maxScore", maxScore);
                    return grade;
                })
                .collect(Collectors.toList());

        logger.info("Fetched {} graded submissions for student ID: {}", grades.size(), student.getId());
        return ResponseEntity.ok(grades);
    } catch (Exception e) {
        logger.error("Error fetching grades: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body(new MessageResponse("Error fetching grades: " + e.getMessage()));
    }
}
}
