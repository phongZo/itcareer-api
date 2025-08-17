package com.base.auth.mapper;

import com.base.auth.dto.review.ReviewClientDto;
import com.base.auth.dto.review.ReviewDto;
import com.base.auth.form.review.CreateReviewForm;
import com.base.auth.form.review.UpdateReviewForm;
import com.base.auth.model.Review;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {StudentMapper.class, SimulationMapper.class})
public interface ReviewMapper {
  @Mapping(source = "star", target = "star")
  @Mapping(source = "comment", target = "comment")
  @BeanMapping(ignoreByDefault = true)
  Review fromCreateReviewFormToEntity(CreateReviewForm createReviewForm);

  @Mapping(source = "star", target = "star")
  @Mapping(source = "comment", target = "comment")
  @Mapping(source = "student", target = "student", qualifiedByName = "fromEntityToStudentDto")
  @Mapping(source = "simulation", target = "simulation", qualifiedByName = "fromEntityToSimulationDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToReviewDto")
  ReviewDto fromEntityToReviewDto(Review review);

  @Mapping(source = "star", target = "star")
  @Mapping(source = "comment", target = "comment")
  @Mapping(source = "student", target = "student", qualifiedByName = "fromStudentToProfileDto")
  @Mapping(source = "simulation", target = "simulation", qualifiedByName = "fromEntityToSimulationDisplayDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToReviewClientDto")
  ReviewClientDto fromEntityToReviewClientDto(Review review);

  @IterableMapping(elementTargetType = ReviewDto.class, qualifiedByName = "fromEntityToReviewDto")
  List<ReviewDto> fromEntityToReviewDtoList(List<Review> reviews);

  @IterableMapping(elementTargetType = ReviewClientDto.class, qualifiedByName = "fromEntityToReviewClientDto")
  List<ReviewClientDto> fromEntityToReviewDtoClientList(List<Review> reviews);

  @Mapping(source = "star", target = "star")
  @Mapping(source = "comment", target = "comment")
  @BeanMapping(ignoreByDefault = true)
  void fromUpdateReviewFormToEntity(UpdateReviewForm updateReviewForm, @MappingTarget Review review);
}
