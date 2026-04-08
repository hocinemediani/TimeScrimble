package com.narbaniki.timescrimble;
import java.util.ArrayList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartieRepository extends JpaRepository<Partie, Integer> {

    ArrayList<Partie> findAllByEstPriveeFalse();
    Partie findByCode(String code);
}
