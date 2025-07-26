package com.base.auth.dto.task;

import com.base.auth.dto.simulation.SimulationClientDto;
import lombok.Data;

@Data
public class TaskDisplayDto {
  private Long id;
  private String name;
  private String description;
  private String content;
  private SimulationClientDto simulation;
}
