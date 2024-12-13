package com.jdgg.catalogo_libros.repositorio;

import com.jdgg.catalogo_libros.modelos.Autores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutoresRepository extends JpaRepository<Autores, Long> {
    Optional<Autores> findByNombreContainingIgnoreCase(String autor);
    @Query("SELECT a FROM Autores a WHERE a.fechaNacimiento <= :ano AND (a.fechaFallecimiento IS NULL OR a.fechaFallecimiento >= :ano)")
    List<Autores> encontrarAutoresEnDeterminadoAno(@Param("ano")int ano);
}