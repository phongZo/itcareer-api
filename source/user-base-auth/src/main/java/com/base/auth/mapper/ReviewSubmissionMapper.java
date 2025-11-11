package com.base.auth.mapper;

import com.base.auth.dto.reviewSubmission.ReviewSubmissionClientDto;
import com.base.auth.dto.reviewSubmission.ReviewSubmissionDto;
import com.base.auth.form.reviewSubmission.CreateReviewSubmissionForm;
import com.base.auth.form.reviewSubmission.UpdateReviewSubmissionForm;
import com.base.auth.model.ReviewSubmission;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {SimulationMapper.class, StudentMapper.class})
public interface ReviewSubmissionMapper {
  @Mapping(source = "content", target = "content")
  @BeanMapping(ignoreByDefault = true)
  ReviewSubmission fromCreateReviewSubmissionToEntity(CreateReviewSubmissionForm createReviewSubmissionForm);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "simulation", target = "simulation", qualifiedByName = "fromEntityToSimulationDto")
  @Mapping(source = "student", target = "student", qualifiedByName = "fromEntityToStudentDto")
  @Mapping(source = "isReviewed", target = "isReviewed")
  @Mapping(source = "createdDate", target = "createdDate")
  @Mapping(source = "modifiedDate", target = "modifiedDate")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToReviewSubmissionDto")
  ReviewSubmissionDto fromEntityToReviewSubmissionDto(ReviewSubmission reviewSubmission);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "simulation", target = "simulation", qualifiedByName = "fromEntityToSimulationDisplayDto")
  @Mapping(source = "student", target = "student", qualifiedByName = "fromStudentToProfileDto")
  @Mapping(source = "isReviewed", target = "isReviewed")
  @Mapping(source = "modifiedDate", target = "modifiedDate")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToReviewSubmissionDtoForClient")
  ReviewSubmissionClientDto fromEntityToReviewSubmissionDtoForClient(ReviewSubmission reviewSubmission);

  @Mapping(source = "content", target = "content")
  @BeanMapping(ignoreByDefault = true)
  void fromUpdateReviewSubmissionToEntity(UpdateReviewSubmissionForm updateReviewSubmissionForm, @MappingTarget ReviewSubmission reviewSubmission);
}
