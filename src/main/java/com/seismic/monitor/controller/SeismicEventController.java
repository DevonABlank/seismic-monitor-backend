package com.seismic.monitor.controller;

import com.seismic.monitor.model.SeismicEvent;
import com.seismic.monitor.service.SeismicEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SeismicEventController {

	private final SeismicEventService service;

	@GetMapping
	public Page<SeismicEvent> getEvents(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) Double minMagnitude) {

		PageRequest pageable = PageRequest.of(page, size, Sort.by("eventTime").descending());
		if (minMagnitude != null) {
			return service.getEventsByMinMagnitude(minMagnitude, pageable);
		}
		return service.getEvents(pageable);
	}

	@GetMapping("/count")
	public long getCount() {
		return service.getTotalCount();
	}
}