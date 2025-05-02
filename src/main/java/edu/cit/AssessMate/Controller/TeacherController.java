package edu.cit.AssessMate.Controller;

import edu.cit.AssessMate.Model.Answer;
import edu.cit.AssessMate.Model.Course;
import edu.cit.AssessMate.Model.Question;
import edu.cit.AssessMate.Model.QuestionOption;
import edu.cit.AssessMate.Model.Quiz;
import edu.cit.AssessMate.Model.QuizSubmission;
import edu.cit.AssessMate.Model.User;
import edu.cit.AssessMate.Model.QuizQuestion;
import edu.cit.AssessMate.payload.request.AnswerFeedback;
import edu.cit.AssessMate.payload.request.CourseRequest;
import edu.cit.AssessMate.payload.request.EnrollStudentRequest;
import edu.cit.AssessMate.payload.request.GradeSubmissionRequest;
import edu.cit.AssessMate.payload.request.QuestionOptionRequest;
import edu.cit.AssessMate.payload.request.QuestionRequest;
import edu.cit.AssessMate.payload.request.QuizRequest;
import edu.cit.AssessMate.payload.response.CountResponse;
import edu.cit.AssessMate.payload.response.CourseDTO;
import edu.cit.AssessMate.payload.response.MessageResponse;
import edu.cit.AssessMate.payload.response.QuizCreationResponse;
import edu.cit.AssessMate.payload.response.QuizDTO;
import edu.cit.AssessMate.Repository.CourseRepository;
import edu.cit.AssessMate.Repository.QuestionOptionRepository;
import edu.cit.AssessMate.Repository.QuestionRepository;
import edu.cit.AssessMate.Repository.QuizRepository;
import edu.cit.AssessMate.Repository.QuizSubmissionRepository;
import edu.cit.AssessMate.Repository.UserRepository;
import edu.cit.AssessMate.Repository.QuizQuestionRepository;
import edu.cit.AssessMate.Service.impl.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherController.class);

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    @GetMapping("/stats/total-enrolled-students")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getTotalEnrolledStudents() {
        logger.info("Attempting to fetch total enrolled students for teacher...");
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long teacherId = userDetails.getId();
            logger.info("Authenticated teacher principal found with ID: {}", teacherId);

            User teacher = userRepository.findById(teacherId).orElseThrow(() -> {
                logger.error("Teacher not found with ID: {}", teacherId);
                return new RuntimeException("Teacher not found");
            });
            logger.info("Teacher entity fetched successfully for ID: {}", teacherId);

            List<Course> courses = courseRepository.findByTeacherWithDetails(teacher);
            long totalEnrolledStudents = courses.stream()
                    .mapToLong(course -> course.getEnrolledStudents().size())
                    .sum();

            logger.info("Successfully calculated total enrolled students: {} for teacher ID: {}", totalEnrolledStudents, teacherId);
            return ResponseEntity.ok(new CountResponse(totalEnrolledStudents));

        } catch (Exception e) {
            logger.error("Error fetching total enrolled students", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error fetching total enrolled students: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/total-courses")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getTotalCourses() {
        logger.info("Attempting to fetch total courses for teacher...");
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long teacherId = userDetails.getId();
            logger.info("Authenticated teacher principal found with ID: {}", teacherId);

            User teacher = userRepository.findById(teacherId).orElseThrow(() -> {
                logger.error("Teacher not found with ID: {}", teacherId);
                return new RuntimeException("Teacher not found");
            });
            logger.info("Teacher entity fetched successfully for ID: {}", teacherId);

            List<Course> courses = courseRepository.findByTeacher(teacher);
            long totalCourses = courses.size();

            logger.info("Successfully calculated total courses: {} for teacher ID: {}", totalCourses, teacherId);
            return ResponseEntity.ok(new CountResponse(totalCourses));

        } catch (Exception e) {
            logger.error("Error fetching total courses", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error fetching total courses: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/total-quizzes")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getTotalQuizzes() {
        logger.info("Attempting to fetch total quizzes for teacher...");
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long teacherId = userDetails.getId();
            logger.info("Authenticated teacher principal found with ID: {}", teacherId);

            User teacher = userRepository.findById(teacherId).orElseThrow(() -> {
                logger.error("Teacher not found with ID: {}", teacherId);
                return new RuntimeException("Teacher not found");
            });
            logger.info("Teacher entity fetched successfully for ID: {}", teacherId);

            List<Course> courses = courseRepository.findByTeacherWithDetails(teacher);
            long totalQuizzes = courses.stream()
                    .mapToLong(course -> quizRepository.findByCourse(course).size())
                    .sum();

            logger.info("Successfully calculated total quizzes: {} for teacher ID: {}", totalQuizzes, teacherId);
            return ResponseEntity.ok(new CountResponse(totalQuizzes));

        } catch (Exception e) {
            logger.error("Error fetching total quizzes", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error fetching total quizzes: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/active-quizzes")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getActiveQuizzes() {
        logger.info("Attempting to fetch active quizzes for teacher...");
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long teacherId = userDetails.getId();
            logger.info("Authenticated teacher principal found with ID: {}", teacherId);

            User teacher = userRepository.findById(teacherId).orElseThrow(() -> {
                logger.error("Teacher not found with ID: {}", teacherId);
                return new RuntimeException("Teacher not found");
            });
            logger.info("Teacher entity fetched successfully for ID: {}", teacherId);

            List<Course> courses = courseRepository.findByTeacherWithDetails(teacher);
            long activeQuizzes = courses.stream()
                    .flatMap(course -> quizRepository.findByCourse(course).stream())
                    .filter(Quiz::getIsActive)
                    .count();

            logger.info("Successfully calculated active quizzes: {} for teacher ID: {}", activeQuizzes, teacherId);
            return ResponseEntity.ok(new CountResponse(activeQuizzes));

        } catch (Exception e) {
            logger.error("Error fetching active quizzes", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error fetching active quizzes: " + e.getMessage()));
        }
    }

    @GetMapping("/courses")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getTeacherCourses() {
        logger.info("Attempting to fetch courses for teacher...");
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long teacherId = userDetails.getId();
            logger.info("Authenticated teacher principal found with ID: {}", teacherId);

            User teacher = userRepository.findById(teacherId).orElseThrow(() -> {
                logger.error("Teacher not found with ID: {}", teacherId);
                return new RuntimeException("Teacher not found");
            });
            logger.info("Teacher entity fetched successfully for ID: {}", teacherId);

            List<Course> fetchedCourses = courseRepository.findByTeacherWithDetails(teacher);
            List<CourseDTO> courseDTOs = new ArrayList<>();

            for (Course course : fetchedCourses) {
                courseDTOs.add(CourseDTO.fromEntity(course));
            }

            logger.info("Successfully converted {} courses to DTOs for teacher ID: {}", courseDTOs.size(), teacherId);
            return ResponseEntity.ok(courseDTOs);

        } catch (Exception e) {
            logger.error("Error fetching courses from repository", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error fetching courses from database: " + e.getMessage()));
        }
    }

    @PostMapping("/courses")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> createCourse(@Valid @RequestBody CourseRequest courseRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User teacher = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Teacher not found"));

        Course course = new Course();
        course.setTitle(courseRequest.getTitle());
        course.setCode(courseRequest.getCode());
        course.setDescription(courseRequest.getDescription());
        course.setTeacher(teacher);

        courseRepository.save(course);

        return ResponseEntity.ok(new MessageResponse("Course created successfully!"));
    }

    @PutMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseRequest courseRequest) {
        try {
            logger.info("Attempting to update course ID: {}", courseId);
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User teacher = userRepository.findById(userDetails.getId()).orElseThrow(() -> {
                logger.error("Teacher not found");
                return new RuntimeException("Teacher not found");
            });

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> {
                        logger.error("Course not found with ID: {}", courseId);
                        return new RuntimeException("Course not found");
                    });

            if (!course.getTeacher().getId().equals(teacher.getId())) {
                logger.warn("Teacher {} does not have permission to modify course {}", teacher.getId(), courseId);
                return ResponseEntity.badRequest().body(new MessageResponse("You don't have permission to modify this course"));
            }

            course.setTitle(courseRequest.getTitle());
            course.setCode(courseRequest.getCode());
            course.setDescription(courseRequest.getDescription());

            courseRepository.save(course);
            logger.info("Course {} updated successfully", courseId);
            return ResponseEntity.ok(new MessageResponse("Course updated successfully!"));
        } catch (Exception e) {
            logger.error("Error updating course ID: {}", courseId, e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error updating course: " + e.getMessage()));
        }
    }

    @DeleteMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            logger.info("Attempting to delete course ID: {}", courseId);
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User teacher = userRepository.findById(userDetails.getId()).orElseThrow(() -> {
                logger.error("Teacher not found");
                return new RuntimeException("Teacher not found");
            });

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> {
                        logger.error("Course not found with ID: {}", courseId);
                        return new RuntimeException("Course not found");
                    });

            if (!course.getTeacher().getId().equals(teacher.getId())) {
                logger.warn("Teacher {} does not have permission to delete course {}", teacher.getId(), courseId);
                return ResponseEntity.badRequest().body(new MessageResponse("You don't have permission to delete this course"));
            }

            courseRepository.delete(course);
            logger.info("Course {} deleted successfully", courseId);
            return ResponseEntity.ok(new MessageResponse("Course deleted successfully!"));
        } catch (Exception e) {
            logger.error("Error deleting course ID: {}", courseId, e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error deleting course: " + e.getMessage()));
        }
    }

    @PostMapping("/courses/{courseId}/students")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> addStudentToCourse(@PathVariable Long courseId, @RequestBody EnrollStudentRequest request) {
        try {
            logger.info("Attempting to enroll student in course ID: {}", courseId);
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User teacher = userRepository.findById(userDetails.getId()).orElseThrow(() -> {
                logger.error("Teacher not found");
                return new RuntimeException("Teacher not found");
            });

            Course course = courseRepository.findByIdWithEnrolledStudents(courseId)
                    .orElseThrow(() -> {
                        logger.error("Course not found with ID: {}", courseId);
                        return new RuntimeException("Course not found");
                    });

            if (!course.getTeacher().getId().equals(teacher.getId())) {
                logger.warn("Teacher {} does not have permission to modify course {}", teacher.getId(), courseId);
                return ResponseEntity.badRequest().body(new MessageResponse("You don't have permission to modify this course"));
            }

            Optional<User> studentOptional = userRepository.findByUsername(request.getUsername());
            if (!studentOptional.isPresent()) {
                logger.warn("Student with username {} not found", request.getUsername());
                return ResponseEntity.badRequest().body(new MessageResponse("Student with username '" + request.getUsername() + "' not found"));
            }

            User student = studentOptional.get();

            if (course.getEnrolledStudents().contains(student)) {
                logger.warn("Student {} is already enrolled in course {}", student.getId(), courseId);
                return ResponseEntity.badRequest().body(new MessageResponse("Student is already enrolled in this course"));
            }

            course.getEnrolledStudents().add(student);
            courseRepository.save(course);
            logger.info("Student {} enrolled in course {} successfully", student.getId(), courseId);
            return ResponseEntity.ok(new MessageResponse("Student added to course successfully!"));
        } catch (Exception e) {
            logger.error("Error enrolling student in course ID: {}", courseId, e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error adding student to course: " + e.getMessage()));
        }
    }

    @GetMapping("/quizzes")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getTeacherQuizzes() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long teacherId = userDetails.getId();
            logger.info("Fetching quizzes for teacher ID: {}", teacherId);

            User teacher = userRepository.findById(teacherId).orElseThrow(() -> {
                logger.error("Teacher not found with ID: {}", teacherId);
                return new RuntimeException("Teacher not found");
            });

            List<Course> courses;
            try {
                courses = courseRepository.findByTeacherWithDetails(teacher);
                logger.info("Found {} courses for teacher", courses.size());
            } catch (Exception e) {
                logger.error("Error fetching courses: {}", e.getMessage(), e);
                return ResponseEntity.internalServerError().body(new MessageResponse("Error fetching courses: " + e.getMessage()));
            }

            List<QuizDTO> quizDTOs = new ArrayList<>();
            for (Course course : courses) {
                try {
                    List<Quiz> quizzes = quizRepository.findByCourse(course);
                    logger.info("Found {} quizzes for course ID: {}", quizzes.size(), course.getId());

                    for (Quiz quiz : quizzes) {
                        quizDTOs.add(new QuizDTO(quiz));
                    }
                } catch (Exception e) {
                    logger.error("Error processing quizzes for course {}: {}", course.getId(), e.getMessage(), e);
                }
            }

            return ResponseEntity.ok(quizDTOs);
        } catch (Exception e) {
            logger.error("Error fetching quizzes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to fetch quizzes: " + e.getMessage()));
        }
    }

    @PostMapping("/courses/{courseId}/quizzes")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> createQuiz(@PathVariable Long courseId, @Valid @RequestBody QuizRequest quizRequest) {
        try {
            logger.info("Creating quiz for course ID: {}", courseId);
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User teacher = userRepository.findById(userDetails.getId()).orElseThrow(() -> {
                logger.error("Teacher not found");
                return new RuntimeException("Teacher not found");
            });

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> {
                        logger.error("Course not found with ID: {}", courseId);
                        return new RuntimeException("Course not found");
                    });

            if (!course.getTeacher().getId().equals(teacher.getId())) {
                logger.warn("Teacher {} does not have permission to modify course {}", teacher.getId(), courseId);
                return ResponseEntity.badRequest().body(new MessageResponse("You don't have permission to modify this course"));
            }

            Quiz quiz = new Quiz();
            quiz.setTitle(quizRequest.getTitle());
            quiz.setDescription(quizRequest.getDescription());
            quiz.setStartTime(quizRequest.getStartTime());
            quiz.setEndTime(quizRequest.getEndTime());
            quiz.setDurationMinutes(quizRequest.getDurationMinutes());
            quiz.setCourse(course);
            Quiz savedQuiz = quizRepository.save(quiz);

            logger.info("Quiz created successfully with ID: {}", savedQuiz.getId());
            QuizCreationResponse response = new QuizCreationResponse("Quiz created successfully!", savedQuiz.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating quiz for course ID: {}", courseId, e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error creating quiz: " + e.getMessage()));
        }
    }

    @PutMapping("/quizzes/{quizId}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> updateQuiz(@PathVariable Long quizId, @Valid @RequestBody QuizRequest quizRequest) {
        try {
            logger.info("Attempting to update quiz ID: {}", quizId);
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User teacher = userRepository.findById(userDetails.getId()).orElseThrow(() -> {
                logger.error("Teacher not found");
                return new RuntimeException("Teacher not found");
            });

            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> {
                        logger.error("Quiz not found with ID: {}", quizId);
                        return new RuntimeException("Quiz not found");
                    });

            if (!quiz.getCourse().getTeacher().getId().equals(teacher.getId())) {
                logger.warn("Teacher {} does not have permission to modify quiz {}", teacher.getId(), quizId);
                return ResponseEntity.badRequest().body(new MessageResponse("You don't have permission to modify this quiz"));
            }

            quiz.setTitle(quizRequest.getTitle());
            quiz.setDescription(quizRequest.getDescription());
            quiz.setStartTime(quizRequest.getStartTime());
            quiz.setEndTime(quizRequest.getEndTime());
            quiz.setDurationMinutes(quizRequest.getDurationMinutes());

            quizRepository.save(quiz);
            logger.info("Quiz {} updated successfully", quizId);
            return ResponseEntity.ok(new MessageResponse("Quiz updated successfully!"));
        } catch (Exception e) {
            logger.error("Error updating quiz ID: {}", quizId, e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error updating quiz: " + e.getMessage()));
        }
    }

    @DeleteMapping("/quizzes/{quizId}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long quizId) {
        try {
            logger.info("Attempting to delete quiz ID: {}", quizId);
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User teacher = userRepository.findById(userDetails.getId()).orElseThrow(() -> {
                logger.error("Teacher not found");
                return new RuntimeException("Teacher not found");
            });

            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> {
                        logger.error("Quiz not found with ID: {}", quizId);
                        return new RuntimeException("Quiz not found");
                    });

            if (!quiz.getCourse().getTeacher().getId().equals(teacher.getId())) {
                logger.warn("Teacher {} does not have permission to delete quiz {}", teacher.getId(), quizId);
                return ResponseEntity.badRequest().body(new MessageResponse("You don't have permission to delete this quiz"));
            }

            quizRepository.delete(quiz);
            logger.info("Quiz {} deleted successfully", quizId);
            return ResponseEntity.ok(new MessageResponse("Quiz deleted successfully!"));
        } catch (Exception e) {
            logger.error("Error deleting quiz ID: {}", quizId, e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error deleting quiz: " + e.getMessage()));
        }
    }

    @PutMapping("/quizzes/{quizId}/activate")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> activateQuiz(@PathVariable Long quizId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User teacher = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Teacher not found"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (!quiz.getCourse().getTeacher().getId().equals(teacher.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("You don't have permission to modify this quiz"));
        }

        quiz.setIsActive(!quiz.getIsActive());
        quizRepository.save(quiz);

        return ResponseEntity.ok(new MessageResponse("Quiz activated successfully!"));
    }

    @GetMapping("/quizzes/{quizId}/submissions")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getQuizSubmissions(@PathVariable Long quizId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User teacher = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Teacher not found"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (!quiz.getCourse().getTeacher().getId().equals(teacher.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("You don't have permission to view this quiz"));
        }

        List<QuizSubmission> submissions = quizSubmissionRepository.findByQuiz(quiz);
        return ResponseEntity.ok(submissions);
    }

    @PutMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> gradeSubmission(@PathVariable Long submissionId, @Valid @RequestBody GradeSubmissionRequest gradeRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User teacher = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Teacher not found"));

        QuizSubmission submission = quizSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (!submission.getQuiz().getCourse().getTeacher().getId().equals(teacher.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("You don't have permission to grade this submission"));
        }

        submission.setScore(gradeRequest.getScore());
        submission.setIsGraded(true);

        if (gradeRequest.getAnswerFeedback() != null && !gradeRequest.getAnswerFeedback().isEmpty()) {
            for (Answer answer : submission.getAnswers()) {
                if (gradeRequest.getAnswerFeedback().containsKey(answer.getId().toString())) {
                    AnswerFeedback feedback = gradeRequest.getAnswerFeedback().get(answer.getId().toString());
                    answer.setPoints(feedback.getPoints());
                    answer.setTeacherFeedback(feedback.getFeedback());
                }
            }
        }

        quizSubmissionRepository.save(submission);

        return ResponseEntity.ok(new MessageResponse("Submission graded successfully!"));
    }

    @PostMapping("/quizzes/{quizId}/questions")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> addQuestionToQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuestionRequest questionRequest) {

        logger.info("Adding question to quiz ID: {}", quizId);
        logger.info("Question type: {}", questionRequest.getType());

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User teacher = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));

            if (!quiz.getCourse().getTeacher().getId().equals(teacher.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("You don't have permission to modify this quiz"));
            }

            Question question = new Question();
            question.setQuestionText(questionRequest.getQuestionText());
            question.setType(questionRequest.getType());
            question.setPoints(questionRequest.getPoints());
            question.setQuiz(quiz);

            Question savedQuestion = questionRepository.save(question);
            logger.info("Saved question with ID: {}", savedQuestion.getId());

            List<QuestionOptionRequest> optionRequests = questionRequest.getOptions();
            if (optionRequests != null && !optionRequests.isEmpty()) {
                logger.info("Processing {} options for question", optionRequests.size());

                for (QuestionOptionRequest optionRequest : optionRequests) {
                    QuestionOption option = new QuestionOption();
                    option.setOptionText(optionRequest.getOptionText());
                    option.setIsCorrect(optionRequest.getIsCorrect());
                    option.setQuestion(savedQuestion);
                    questionOptionRepository.save(option);
                    logger.info("Saved option: {}, isCorrect: {}", optionRequest.getOptionText(), optionRequest.getIsCorrect());
                }
            } else {
                switch (questionRequest.getType()) {
                    case TRUE_FALSE:
                        logger.info("Creating default True/False options");
                        QuestionOption trueOption = new QuestionOption();
                        trueOption.setOptionText("True");
                        trueOption.setIsCorrect(false);
                        trueOption.setQuestion(savedQuestion);

                        QuestionOption falseOption = new QuestionOption();
                        falseOption.setOptionText("False");
                        falseOption.setIsCorrect(false);
                        falseOption.setQuestion(savedQuestion);

                        questionOptionRepository.save(trueOption);
                        questionOptionRepository.save(falseOption);
                        break;

                    case SHORT_ANSWER:
                        logger.info("Creating placeholder for Short Answer question");
                        QuestionOption expectedAnswer = new QuestionOption();
                        expectedAnswer.setOptionText("");
                        expectedAnswer.setIsCorrect(true);
                        expectedAnswer.setQuestion(savedQuestion);
                        questionOptionRepository.save(expectedAnswer);
                        break;

                    default:
                        logger.info("No options needed for question type: {}", questionRequest.getType());
                        break;
                }
            }

            return ResponseEntity.ok(new MessageResponse("Question added successfully!"));
        } catch (Exception e) {
            logger.error("Error adding question to quiz: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to add question: " + e.getMessage()));
        }
    }
    @GetMapping("/quizzes/{quizId}/questions")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getQuizQuestions(@PathVariable Long quizId) {
        try {
            logger.info("Fetching questions for quiz ID: {}", quizId);
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User teacher = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> {
                        logger.error("Teacher not found");
                        return new RuntimeException("Teacher not found");
                    });

            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> {
                        logger.error("Quiz not found with ID: {}", quizId);
                        return new RuntimeException("Quiz not found");
                    });

            if (quiz.getCourse() == null || quiz.getCourse().getTeacher() == null || !quiz.getCourse().getTeacher().getId().equals(teacher.getId())) {
                logger.warn("Teacher {} does not have permission to view questions for quiz {}", teacher.getId(), quizId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("You don't have permission to view these questions"));
            }

            List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);
            logger.info("Fetched {} questions for quiz ID: {}", questions.size(), quizId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            logger.error("Error fetching questions for quiz ID: {}", quizId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching questions: " + e.getMessage()));
        }
    }

    @PutMapping("/quizzes/{quizId}/questions/{questionId}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> updateQuizQuestion(@PathVariable Long quizId, @PathVariable Long questionId, @Valid @RequestBody QuestionRequest questionRequest) {
        try {
            logger.info("Attempting to update question ID: {} for quiz ID: {}", questionId, quizId);
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User teacher = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> {
                        logger.error("Teacher not found");
                        return new RuntimeException("Teacher not found");
                    });

            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> {
                        logger.error("Quiz not found with ID: {}", quizId);
                        return new RuntimeException("Quiz not found");
                    });

            if (quiz.getCourse() == null || quiz.getCourse().getTeacher() == null || !quiz.getCourse().getTeacher().getId().equals(teacher.getId())) {
                logger.warn("Teacher {} does not have permission to update question for quiz {}", teacher.getId(), quizId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("You don't have permission to update this question"));
            }

            QuizQuestion question = quizQuestionRepository.findById(questionId)
                    .orElseThrow(() -> {
                        logger.error("Question not found with ID: {}", questionId);
                        return new RuntimeException("Question not found");
                    });

            if (!question.getQuiz().getId().equals(quizId)) {
                logger.warn("Question {} does not belong to quiz {}", questionId, quizId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Question does not belong to this quiz"));
            }

            question.setText(questionRequest.getText());
            question.setOptions(questionRequest.getOptions());
            question.setCorrectAnswer(questionRequest.getCorrectAnswer());
            question.setPoints(questionRequest.getPoints());
            quizQuestionRepository.save(question);

            logger.info("Question {} updated successfully for quiz ID: {}", questionId, quizId);
            return ResponseEntity.ok(new MessageResponse("Question updated successfully!"));
        } catch (Exception e) {
            logger.error("Error updating question ID: {} for quiz ID: {}", questionId, quizId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating question: " + e.getMessage()));
        }
    }
}
