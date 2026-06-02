package com.seismic.monitor.repository;

import com.seismic.monitor.model.SeismicEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeismicEventRepository extends JpaRepository<SeismicEvent, Long> {
	Page<SeismicEvent> findByMagnitudeGreaterThanEqual(Double magnitude, Pageable pageable);
	boolean existsByUsgsId(String usgsId);
}