package com.example.fly.demo.adapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;



@Controller
public class GameWeb {
    
    private static final String COOKIE_NAME = "GAME_ID";
    public static final String SPEED_MODE = "SPEED";
    public static final String ENDURANCE_MODE = "ENDURANCE";

    private static GameLocation speedLocation() {
        return GameLocation.builder()
            .gameId(UUID.randomUUID().toString())
            // Fosa de las marianas
            .latitud(BigDecimal.valueOf(11.3493))
            .longitud(BigDecimal.valueOf(142.1996))
            .distance(BigDecimal.valueOf(0))
            .elevation(BigDecimal.valueOf(-10745))
            .points(BigDecimal.valueOf(0))
            .totalPoints(BigDecimal.valueOf(0))
            .steps(BigDecimal.valueOf(0))
            .totalDistance(BigDecimal.valueOf(0))
            .totalTime(0)
            .mode(SPEED_MODE)
            .build();
    }

    private static GameLocation enduranceLocation() {
        return GameLocation.builder()
            .gameId(UUID.randomUUID().toString())
            // Fosa de las marianas
            .latitud(BigDecimal.valueOf(11.3493))
            .longitud(BigDecimal.valueOf(142.1996))
            .distance(BigDecimal.valueOf(0))
            .elevation(BigDecimal.valueOf(-10745))
            .points(BigDecimal.valueOf(0))
            .totalPoints(BigDecimal.valueOf(0))
            .steps(BigDecimal.valueOf(0))
            .totalDistance(BigDecimal.valueOf(0))
            .totalTime(0)
            .mode(ENDURANCE_MODE)
            .build();
    }

    @Value("${apimode:true}")
    private boolean apimode;
    private JPAGameRepository jpaGameRepository;
    private RestTemplate restTemplate;
    private HashMap<String,GameLocation> locations = new HashMap<>();

    public GameWeb(JPAGameRepository jpaGameRepository, RestTemplate restTemplate) {
        this.jpaGameRepository = jpaGameRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("speedgames", 
            jpaGameRepository.findAll().stream()
                .filter(g->SPEED_MODE.equals(g.getMode()))
                .sorted((a,b) -> b.totalPoints().compareTo(a.totalPoints()))
                .limit(10)
                .collect(Collectors.toList()));
        model.addAttribute("endurancegames", 
            jpaGameRepository.findAll().stream()
                .filter(g->ENDURANCE_MODE.equals(g.getMode()))
                .sorted((a,b) -> b.totalPoints().compareTo(a.totalPoints()))
                .limit(10)
                .collect(Collectors.toList()));
        return "index";
    }

    @GetMapping("/speed")
    public String speed(Model model, HttpServletResponse response) {
        GameLocation home = speedLocation();
        String gameId = home.getGameId();
        Cookie gameCookie = new Cookie(COOKIE_NAME ,gameId);
        gameCookie.setSecure(true);
        gameCookie.setHttpOnly(true);
        response.addCookie(gameCookie);
        locations.put(gameId,home);
        model.addAttribute("current", home);
        model.addAttribute("mode", home.getMode());
        jpaGameRepository.saveAndFlush(Game.fromModel(home));
        return "game";
    }

    @GetMapping("/endurance")
    public String endurance(Model model, HttpServletResponse response) {
        GameLocation home = enduranceLocation();
        String gameId = home.getGameId();
        Cookie gameCookie = new Cookie(COOKIE_NAME ,gameId);
        gameCookie.setSecure(true);
        gameCookie.setHttpOnly(true);
        response.addCookie(gameCookie);
        locations.put(gameId,home);
        model.addAttribute("current", home);
        model.addAttribute("mode", home.getMode());
        jpaGameRepository.saveAndFlush(Game.fromModel(home));
        return "game";
    }

    @GetMapping("/location/{lat}/{lng}")
    public ResponseEntity<GameLocation> location(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "lat", required = true) float lat, @PathVariable(name = "lng", required = true) float lng) {
        // https://api.opentopodata.org/v1/gebco2020?locations=${e.latlng.lat},${e.latlng.lng}
        // Max 100 locations per request.
        // Max 1 call per second.
        // Max 1000 calls per day
        Cookie gameCookie = Arrays.stream(request.getCookies()).filter(c -> COOKIE_NAME.equals(c.getName())).findFirst().orElseThrow();
        GameLocation currentLocation = locations.get(gameCookie.getValue());
        Topo tp = apimode ? 
            restTemplate.getForObject(String.format(java.util.Locale.ENGLISH, "https://api.opentopodata.org/v1/gebco2020?locations=%f,%f",lat,lng),Topo.class)
            : Topo.random(currentLocation.getElevation());
        GameLocation newLocation = GameLocation
            .builder()
            .latitud(BigDecimal.valueOf(lat))
            .longitud(BigDecimal.valueOf(lng))
            .elevation(BigDecimal.valueOf(tp.getElevation()))
            .steps(currentLocation.getSteps().add(BigDecimal.valueOf(1)))
            .lastTime(System.currentTimeMillis())
            .mode(currentLocation.getMode())
            .build();

        newLocation.calculatePointsAndDistance(currentLocation, BigDecimal.valueOf(lat), BigDecimal.valueOf(lng));
        
        Optional<Game> dbGame = jpaGameRepository.findById(gameCookie.getValue());
        if (dbGame.isPresent()) {
            dbGame.get().addStep(newLocation);
            jpaGameRepository.saveAndFlush(dbGame.get());
        }

        if (newLocation.gameOver()) {
            Cookie cookie = new Cookie(COOKIE_NAME, null);
            cookie.setMaxAge(0);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            return ResponseEntity.internalServerError().body(newLocation);
        } else {
            locations.put(gameCookie.getValue(),newLocation);
            return ResponseEntity.ok().body(newLocation);
        } 
    }

}
