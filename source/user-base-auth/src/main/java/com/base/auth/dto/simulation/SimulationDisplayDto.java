package com.base.auth.dto.simulation;

import com.base.auth.dto.educator.ProfileEducatorDto;
import lombok.Data;

@Data
public class SimulationDisplayDto {
  private Long id;
  private String title;
  private Integer level;
  private String totalEstimatedTime;
  private Integer participantQuantity;
  private String imagePath;
  private Float avgRating;
  private Float percent;
  private int status;
  private ProfileEducatorDto educator;
}
