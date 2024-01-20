package com.example.fly.demo.adapter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="games")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Game {
    
    @Id
	private String id;

    @NotNull
    @NotBlank
	@Column(name = "mode")
	private String mode;

    @NotNull
	@Column(name = "start")
	private LocalDateTime start;

	@OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<GameStep> steps;

	public static Game fromModel(GameLocation gl) {
		Game g = Game.builder()
			.id(gl.getGameId())
			.mode(gl.getMode())
			.start(LocalDateTime.now())
			.build();
		g.addStep(gl);
		return g;
	}

	public void addStep(GameLocation gl) {
		if (this.steps==null) this.steps = new ArrayList<>();
		GameStep gs = GameStep.fromModel(this, gl, this.steps.size() + 1);
		this.steps.add(gs);
	}

	public int totalSteps() {
		return this.steps.size();
	}

	public BigDecimal totalPoints() {
		return this.steps.stream().map(GameStep::getPoints).reduce(BigDecimal.valueOf(0), (a,b) -> a.add(b) );
	}

	public BigDecimal totalDistance() {
		return this.steps.stream().map(GameStep::getDistance).reduce(BigDecimal.valueOf(0), (a,b) -> a.add(b) );
	}

	public String totalTime() {
		LocalDateTime ini = this.steps.stream().filter(g -> g.getStep() == 1).map(GameStep::getTime).findFirst().orElse(null);
		LocalDateTime fin = this.steps.stream().filter(g -> g.getStep() == totalSteps() ).map(GameStep::getTime).findFirst().orElse(null);
		Duration diff = Duration.between(ini, fin);
		return String.format("%sh:%sm:%ss",diff.toHoursPart(),diff.toMinutesPart(),diff.toSecondsPart());
	}

	public BigDecimal maxElevation() {
		return this.steps.stream().map(GameStep::getElevation).reduce(BigDecimal.valueOf(0), (a,b) -> a.compareTo(b) > 0 ? a:b );
	}

	public String formatStart() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		return this.start.format(formatter);
	}
}
