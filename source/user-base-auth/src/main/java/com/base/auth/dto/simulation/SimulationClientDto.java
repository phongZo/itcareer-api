package com.base.auth.dto.simulation;

import com.base.auth.dto.educator.EducatorAutoCompleteDto;
import com.base.auth.dto.educator.ProfileEducatorDto;
import com.base.auth.dto.specialization.SpecializationAutoCompleteDto;
import lombok.Data;

@Data
public class SimulationClientDto {
  private String title;
  private String overview;
  private String description;
  private Integer level;
  private String totalEstimatedTime;
  private String imagePath;
  private String videoPath;
  private Float avgRating;
  private Integer participantQuantity;
  private SpecializationAutoCompleteDto specialization;
  private ProfileEducatorDto educator;
}
