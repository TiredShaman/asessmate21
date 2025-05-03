package edu.cit.AssessMate.payload.response;

import edu.cit.AssessMate.Model.Course;
import lombok.Getter;
import lombok.Setter;

public class CourseDTO {
    @Getter @Setter private Long id;
    @Getter @Setter private String title;
    @Getter @Setter private String code;
    @Getter @Setter private String description;
    @Getter @Setter private Long numberOfEnrolledStudents;

    // Static factory method to convert from entity to DTO
    public static CourseDTO fromEntity(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setCode(course.getCode());
        dto.setDescription(course.getDescription());
        dto.setNumberOfEnrolledStudents((long) course.getEnrolledStudents().size());
        return dto;
    }
}
