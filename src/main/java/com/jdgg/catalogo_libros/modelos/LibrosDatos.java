package com.jdgg.catalogo_libros.modelos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LibrosDatos(@JsonAlias("title") String titulo,
                          @JsonAlias("authors") List<AutoresDatos> autores,
                          @JsonAlias("languages") List<String> idiomas,
                          @JsonAlias("download_count") int descargas) {

}
