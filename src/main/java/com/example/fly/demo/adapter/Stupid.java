package com.example.fly.demo.adapter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="stupid")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Stupid {
    
    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

    @NotNull
    @NotBlank
	@Column(name = "name")
	private String name;

    @NotNull
    @NotBlank
	@Column(name = "email")
	private String email;

}
