package com.base.auth.controller;

import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.dto.ResponseListDto;
import com.base.auth.dto.review.ReviewClientDto;
import com.base.auth.dto.review.ReviewDto;
import com.base.auth.exception.BadRequestException;
import com.base.auth.exception.NotFoundException;
import com.base.auth.form.review.CreateReviewForm;
import com.base.auth.mapper.ReviewMapper;
import com.base.auth.model.Review;
import com.base.auth.model.Simulation;
import com.base.auth.model.Student;
import com.base.auth.model.criteria.ReviewCriteria;
import com.base.auth.repository.ReviewRepository;
import com.base.auth.repository.SimulationRepository;
import com.base.auth.repository.StudentRepository;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/review")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ReviewController extends ABasicController{
  @Autowired
  ReviewRepository reviewRepository;

  @Autowired
  ReviewMapper reviewMapper;

  @Autowired
  StudentRepository studentRepository;

  @Autowired
  SimulationRepository simulationRepository;

  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('RE_C')")
  public ApiMessageDto<String> create(@Valid @RequestBody CreateReviewForm createReviewForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    Student student = studentRepository.findById(getCurrentUser()).orElseThrow(()
    -> new NotFoundException("Student not found", ErrorCode.USER_ERROR_NOT_FOUND));
    if (!isStudent()){
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    Simulation simulation = simulationRepository.findById(createReviewForm.getSimulationId()).orElseThrow(()
    -> new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND));
    int totalReviewer = reviewRepository.countBySimulationId(createReviewForm.getSimulationId());
    Review review = reviewMapper.fromCreateReviewFormToEntity(createReviewForm);
    review.setStudent(student);
    review.setSimulation(simulation);
    reviewRepository.save(review);
    if (totalReviewer > 0){
      float avgRating = ((simulation.getAvgRating() * totalReviewer) + review.getStar()) / (totalReviewer + 1);
      simulation.setAvgRating(avgRating);
    } else {
      simulation.setAvgRating((float) createReviewForm.getStar());
    }
    simulationRepository.save(simulation);
    apiMessageDto.setMessage("create success");
    return apiMessageDto;
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('RE_L')")
  public ApiMessageDto<ResponseListDto<List<ReviewDto>>> getList(ReviewCriteria reviewCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<ReviewDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<ReviewDto>> responseListDto = new ResponseListDto<>();
    Page<Review> reviews = reviewRepository.findAll(reviewCriteria.getSpecification(), pageable);
    responseListDto.setContent(reviewMapper.fromEntityToReviewDtoList(reviews.getContent()));
    responseListDto.setTotalElements(reviews.getTotalElements());
    responseListDto.setTotalPages(reviews.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("get list success");
    return apiMessageDto;
  }

  @GetMapping(value = "client-list", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<ResponseListDto<List<ReviewClientDto>>> getListForClient(ReviewCriteria reviewCriteria, Pageable pageable){
    ApiMessageDto<ResponseListDto<List<ReviewClientDto>>> apiMessageDto = new ApiMessageDto<>();
    ResponseListDto<List<ReviewClientDto>> responseListDto = new ResponseListDto<>();
    Page<Review> reviews = reviewRepository.findAll(reviewCriteria.getSpecification(), pageable);
    responseListDto.setContent(reviewMapper.fromEntityToReviewDtoClientList(reviews.getContent()));
    responseListDto.setTotalElements(reviews.getTotalElements());
    responseListDto.setTotalPages(reviews.getTotalPages());
    apiMessageDto.setData(responseListDto);
    apiMessageDto.setMessage("get list success");
    return apiMessageDto;
  }

  @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<String> delete(@PathVariable("id") Long id){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    if (!isStudent()){
      throw new BadRequestException("User is not a student", ErrorCode.USER_ERROR_NOT_STUDENT);
    }
    Review review = reviewRepository.findById(id).orElseThrow(()
    -> new NotFoundException("Review not found", ErrorCode.REVIEW_NOT_FOUND));
    if (!review.getStudent().getId().equals(getCurrentUser())){
      throw new BadRequestException("Review cannot deleted", ErrorCode.REVIEW_NOT_AUTHORIZE);
    }
    Simulation simulation = review.getSimulation();
    if (simulation == null){
      throw new NotFoundException("Simulation not found", ErrorCode.SIMULATION_ERROR_NOT_FOUND);
    }
    int totalReviewer = reviewRepository.countBySimulationId(simulation.getId());
    reviewRepository.delete(review);
    if (totalReviewer > 1){
      float avgRating = ((simulation.getAvgRating() * totalReviewer) - review.getStar()) / (totalReviewer - 1);
      simulation.setAvgRating(avgRating);
    } else {
      simulation.setAvgRating(0F);
    }
    simulationRepository.save(simulation);
    apiMessageDto.setMessage("delete success");
    return apiMessageDto;
  }
}
