package com.base.auth.repository;

import com.base.auth.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AchievementRepository extends JpaRepository<Achievement, Long>,
    JpaSpecificationExecutor<Achievement> {

}
