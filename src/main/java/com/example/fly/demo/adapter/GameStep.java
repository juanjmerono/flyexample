package com.example.fly.demo.adapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="steps")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class GameStep {
   
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@Column(columnDefinition = "serial")
	private Long id;	
	
    @ManyToOne
    @JoinColumn(name = "gameid")
	private Game game;

    @NotNull
	@Column(name = "step")
	private int step;

    @NotNull
	@Column(name = "latitud")
	private BigDecimal latitud;

    @NotNull
	@Column(name = "longitud")
	private BigDecimal longitud;

    @NotNull
	@Column(name = "distance")
	private BigDecimal distance;

    @NotNull
	@Column(name = "elevation")
	private BigDecimal elevation;

    @NotNull
	@Column(name = "time")
	private LocalDateTime time;

    @NotNull
	@Column(name = "points")
	private BigDecimal points;

	public static GameStep fromModel(Game g, GameLocation gl, int step) {
		return GameStep.builder()
			.game(g)
			.step(step)
			.latitud(gl.getLatitud())
			.longitud(gl.getLongitud())
			.distance(gl.getDistance())
			.elevation(gl.getElevation())
			.time(LocalDateTime.now())
			.points(gl.getPoints())
			.build();
	}

	public String toString() {
		return String.format("[%s][%s][%s][%s][%s][%s][%s][%s][%s]",id,game.getId(),step,latitud,longitud,distance,elevation,time,points);
	}

}
