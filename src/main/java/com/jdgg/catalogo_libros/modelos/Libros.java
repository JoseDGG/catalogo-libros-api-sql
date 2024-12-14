package com.jdgg.catalogo_libros.modelos;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "libros")
public class Libros {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    @Column
    private String idiomas;
    private int descargas;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "libros_autores",
            joinColumns = @JoinColumn(name = "libro_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id", referencedColumnName = "id")
    )
    //owner of the relationship
    private List<Autores> autores = new ArrayList<>();

    public Libros(){}

    public Libros(String titulo, List<String> idiomas, int descargas) {
        this.titulo = titulo;
        this.idiomas = String.join(", ",idiomas);
        this.descargas = descargas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIdiomas() {
        return idiomas;
    }

    public void setIdiomas(String idiomas) {
        this.idiomas = idiomas;
    }

    public int getDescargas() {
        return descargas;
    }

    public void setDescargas(int descargas) {
        this.descargas = descargas;
    }

    public List<Autores> getAutores() {
        return autores;
    }

    public void setAutores(List<Autores> autores) {
        this.autores = autores;
    }

    public void addAutor(Autores autor) {
        this.autores.add(autor);
        autor.getLibros().add(this);
    }

    public void addAutorList(List<Autores> autores){
      this.autores = autores;
      autores.forEach(a->a.getLibros().add(this));
    }

    private String autoresNombres (){
        return autores.stream().map(Autores::getAutor).collect(Collectors.joining(", "));
    }

    private String getIdiomasLegible(String idiomas){
        if (idiomas == null || idiomas.isBlank()) {
            return "";
        }
        List<String> idioma = List.of(idiomas.split(",\\s*"));
        return idioma.stream().map(Idiomas::idiomaLegible).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return  String.format(
                "----------Libro-----------%n" +
                "titulo: %s%n" +
                "autor: %s%n" +
                "idiomas: %s%n" +
                "descargas totales: %s%n" +
                "--------------------------%n", titulo, autoresNombres(), getIdiomasLegible(idiomas), descargas);
    }
}
