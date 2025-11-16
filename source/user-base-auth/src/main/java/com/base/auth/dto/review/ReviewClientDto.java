package com.base.auth.dto.review;

import com.base.auth.dto.simulation.SimulationDisplayDto;
import com.base.auth.dto.student.ProfileStudentDto;
import lombok.Data;

@Data
public class ReviewClientDto {
  private Long id;
  private Integer star;
  private String comment;
  private ProfileStudentDto student;
  private SimulationDisplayDto simulation;
}
