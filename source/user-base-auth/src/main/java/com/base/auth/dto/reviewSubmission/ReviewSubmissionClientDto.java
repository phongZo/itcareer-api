package com.base.auth.dto.reviewSubmission;

import com.base.auth.dto.simulation.SimulationDisplayDto;
import com.base.auth.dto.student.ProfileStudentDto;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReviewSubmissionClientDto {
  private Long id;
  private String content;
  private SimulationDisplayDto simulation;
  private ProfileStudentDto student;
  private Boolean isReviewed;
  private LocalDateTime modifiedDate;
}
