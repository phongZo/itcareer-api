package com.base.auth.dto.reviewSubmission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewedStudentProjection {
  private String username;
  private Boolean isReviewed;
}
