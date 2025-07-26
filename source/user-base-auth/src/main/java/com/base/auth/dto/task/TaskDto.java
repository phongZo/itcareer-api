package com.base.auth.dto.task;

import com.base.auth.dto.simulation.SimulationDto;
import lombok.Data;

@Data
public class TaskDto {
  private Long id;
  private String name;
  private String description;
  private String content;
  private SimulationDto simulation;
}
