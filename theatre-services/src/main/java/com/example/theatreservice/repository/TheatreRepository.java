package com.example.theatreservice.repository;

import com.example.theatreservice.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    List<Theatre> findByCityIgnoreCaseOrderByNameAsc(String city);
}
