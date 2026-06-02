package com.seismic.monitor.service;

import com.seismic.monitor.model.SeismicEvent;
import com.seismic.monitor.repository.SeismicEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeismicEventService {

	private final SeismicEventRepository repository;
	private final WebClient webClient = WebClient.create("https://earthquake.usgs.gov");

	@Scheduled(fixedRate = 60000)
	public void fetchAndStore() {
		log.info("Fetching USGS earthquake data...");
		try {
			JsonNode root = webClient.get()
					.uri("/earthquakes/feed/v1.0/summary/all_day.geojson")
					.retrieve()
					.bodyToMono(JsonNode.class)
					.block();

			if (root == null) return;

			for (JsonNode feature : root.path("features")) {
				String usgsId = feature.path("id").asText();
				if (repository.existsByUsgsId(usgsId)) continue;

				JsonNode props = feature.path("properties");
				JsonNode coords = feature.path("geometry").path("coordinates");

				SeismicEvent event = new SeismicEvent();
				event.setUsgsId(usgsId);
				event.setPlace(props.path("place").asText());
				event.setMagnitude(props.path("mag").isNull() ? null : props.path("mag").asDouble());
				event.setDepth(coords.size() > 2 ? coords.get(2).asDouble() : null);
				event.setLongitude(coords.size() > 0 ? coords.get(0).asDouble() : null);
				event.setLatitude(coords.size() > 1 ? coords.get(1).asDouble() : null);
				event.setAlert(props.path("alert").asText(null));
				event.setTsunami(props.path("tsunami").asInt() == 1);
				event.setSignificance(props.path("sig").asInt());
				event.setEventTime(Instant.ofEpochMilli(props.path("time").asLong()));
				event.setFetchedAt(Instant.now());

				repository.save(event);
			}
			log.info("USGS fetch complete. Total events: {}", repository.count());
		} catch (Exception e) {
			log.error("Failed to fetch USGS data", e);
		}
	}

	public Page<SeismicEvent> getEvents(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public Page<SeismicEvent> getEventsByMinMagnitude(Double minMag, Pageable pageable) {
		return repository.findByMagnitudeGreaterThanEqual(minMag, pageable);
	}

	public long getTotalCount() { return repository.count(); }
}