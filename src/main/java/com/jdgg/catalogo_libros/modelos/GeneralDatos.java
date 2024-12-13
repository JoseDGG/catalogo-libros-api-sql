package com.jdgg.catalogo_libros.modelos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeneralDatos(@JsonAlias("count") String cantidadDeLibros,
                           @JsonAlias("results") List<LibrosDatos> resultados){

}
