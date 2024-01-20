package com.example.fly.demo.adapter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Topo {
    
    private List<Result> results;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Result {

        private String dataset;
        private float elevation;

    }

    public float getElevation() {
        return results.get(0).getElevation();
    }

    public static Topo random(BigDecimal alt) {
        int pct = RandomGenerator.getDefault().nextInt(100);
        int delta = pct > 70 ? -100 : 100;
        List<Result> rs = Arrays.asList(Result.builder().elevation(alt.floatValue() + delta).build());
        return Topo.builder().results(rs).build();
    }

}
