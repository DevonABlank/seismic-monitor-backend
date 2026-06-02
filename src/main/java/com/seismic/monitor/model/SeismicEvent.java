package com.seismic.monitor.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "seismic_events")
public class SeismicEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String usgsId;
	private String place;
	private Double magnitude;
	private Double depth;
	private Double latitude;
	private Double longitude;
	private String alert;
	private Boolean tsunami;
	private Integer significance;
	private Instant eventTime;
	private Instant fetchedAt;
}
