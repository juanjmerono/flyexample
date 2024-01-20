package com.example.fly.demo.adapter;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JPAGameRepository extends JpaRepository <Game, String> {
    
}
