package com.base.auth.dto.simulation;

import com.base.auth.dto.educator.EducatorDto;
import com.base.auth.dto.specialization.SpecializationDto;
import lombok.Data;

@Data
public class SimulationDto {
  private Long id;
  private String title;
  private String overview;
  private String description;
  private Integer level;
  private String totalEstimatedTime;
  private String imagePath;
  private String videoPath;
  private Float avgRating;
  private Integer participantQuantity;
  private int status;
  private SpecializationDto specialization;
  private EducatorDto educator;
}
