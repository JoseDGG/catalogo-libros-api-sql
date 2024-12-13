package com.jdgg.catalogo_libros.modelos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AutoresDatos(@JsonAlias("name") String autor,
                           @JsonAlias("birth_year") int fechaNacimiento,
                           @JsonAlias("death_year") int fechaFallecimiento) {
}
