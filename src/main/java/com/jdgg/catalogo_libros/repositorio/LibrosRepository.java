package com.jdgg.catalogo_libros.repositorio;

import com.jdgg.catalogo_libros.modelos.Libros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibrosRepository extends JpaRepository<Libros, Long> {
    Optional<Libros> findByTituloContainingIgnoreCase(String tituloBuscar);
    @Query("SELECT a FROM Libros a WHERE a.idiomas = :idioma")
    List<Libros> buscarLibrosPorIdioma(@Param("idioma")String idioma);
}
