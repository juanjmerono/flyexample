package com.example.fly.demo.adapter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class GameLocation {
    
    @JsonProperty("lat")
    private BigDecimal latitud;
    @JsonProperty("lng")
    private BigDecimal longitud;
    @JsonProperty("alt")
    private BigDecimal elevation;
    @JsonProperty("dst")
    private BigDecimal distance;
    @JsonProperty("pts")
    private BigDecimal points;
    @JsonProperty("stp")
    private BigDecimal steps;
    @JsonProperty("totaldst")
    private BigDecimal totalDistance;    
    @JsonProperty("totalpts")
    private BigDecimal totalPoints;
    @JsonProperty("time")
    private long totalTime;
    @JsonIgnore
    private long lastTime;
    @JsonIgnore
    private boolean gameOver = false;
    @JsonIgnore
    private String mode;
    @JsonIgnore
    private String gameId;

    private static final float SPEED_DISTANCE_WEIGHT = 0.1f;
    private static final float SPEED_TIME_WEIGHT = Math.abs(1 - SPEED_DISTANCE_WEIGHT);

    private static final float ENDURANCE_DISTANCE_WEIGHT = 0.8f;
    private static final float ENDURANCE_TIME_WEIGHT = Math.abs(1 - ENDURANCE_DISTANCE_WEIGHT);

    // Haversine formula
    protected static BigDecimal locationDistance(GameLocation loc1, BigDecimal nlat2, BigDecimal nlng2) {
        double lat1 = loc1.getLatitud().doubleValue();
        double lon1 = loc1.getLongitud().doubleValue();
        double lat2 = nlat2.doubleValue();
        double lon2 = nlng2.doubleValue();
        double deg2rad = Math.PI / 180.0;
        double R = 6371; // Radio de la Tierra en kilómetros
        double dLat = deg2rad * (lat2 - lat1);  // deg2rad a continuación
        double dLon = deg2rad * (lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(deg2rad * lat1) * Math.cos(deg2rad * lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Distancia en kilómetros
        return BigDecimal.valueOf(distance);
    }

    public void calculatePointsAndDistance(GameLocation previous, BigDecimal lat, BigDecimal lng) {
        this.distance = locationDistance(previous, lat, lng);
        this.gameOver = this.elevation.compareTo(previous.getElevation()) < 0;
        this.totalDistance = previous.getTotalDistance().add(this.distance);
        this.totalTime = previous.getTotalTime() + (this.defaultLastTime() - previous.defaultLastTime()) + 10;
        BigDecimal distancePoints = totalDistance.multiply(BigDecimal.valueOf(getDistanceWeight()));
        BigDecimal timePoints = BigDecimal.valueOf((1/totalTime) * getTimeWeight());
        this.points = !this.gameOver?distancePoints.add(timePoints).setScale(2, RoundingMode.FLOOR):BigDecimal.valueOf(0);
        this.totalPoints = previous.getTotalPoints().add(this.points).setScale(2, RoundingMode.FLOOR);
    }    

    public boolean gameOver() { return gameOver; }
    public long defaultLastTime() { return this.lastTime == 0 ? System.currentTimeMillis() : this.lastTime; }
    public float getDistanceWeight() {
        switch (this.mode) {
            case GameWeb.SPEED_MODE:
                return SPEED_DISTANCE_WEIGHT;
            case GameWeb.ENDURANCE_MODE:
                return ENDURANCE_DISTANCE_WEIGHT;
            default:
                return 0;
        }
    }
    public float getTimeWeight() {
        switch (this.mode) {
            case GameWeb.SPEED_MODE:
                return SPEED_TIME_WEIGHT;
            case GameWeb.ENDURANCE_MODE:
                return ENDURANCE_TIME_WEIGHT;
            default:
                return 0;
        }
    }

}
