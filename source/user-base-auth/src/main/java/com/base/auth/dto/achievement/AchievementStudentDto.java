package com.base.auth.dto.achievement;

import com.base.auth.dto.simulation.SimulationDisplayDto;
import lombok.Data;

@Data
public class AchievementStudentDto {
  private Long id;
  private String filePath;
  private SimulationDisplayDto simulation;
}
