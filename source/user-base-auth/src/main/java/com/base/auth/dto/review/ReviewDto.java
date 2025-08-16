package com.base.auth.dto.review;

import com.base.auth.dto.simulation.SimulationDto;
import com.base.auth.dto.student.StudentDto;
import lombok.Data;

@Data
public class ReviewDto {
  private Integer star;
  private String comment;
  private StudentDto student;
  private SimulationDto simulation;
}
