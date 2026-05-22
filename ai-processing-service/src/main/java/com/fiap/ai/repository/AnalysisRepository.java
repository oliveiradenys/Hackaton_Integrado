package com.fiap.ai.repository;

import com.fiap.ai.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    Optional<Analysis> findFirstByFileNameOrderByIdDesc(String fileName);
}
