package com.jdgg.catalogo_libros.modelos;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "autores")
public class Autores {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    private String nombre;
    @Column
    private int fechaNacimiento;
    @Column
    private int fechaFallecimiento;
    //non owner of the relationship due to mappedBy
    @ManyToMany(mappedBy = "autores", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Libros> libros = new ArrayList<>();

    public Autores(){}

    public Autores(String nombre, int fechaNacimiento, int fechaFallecimiento){
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaFallecimiento = fechaFallecimiento;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAutor() {
        return nombre;
    }

    public void setAutor(String autor) {
        this.nombre = autor;
    }

    public int getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(int fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getFechaFallecimiento() {
        return fechaFallecimiento;
    }

    public void setFechaFallecimiento(int fechaFallecimiento) {
        this.fechaFallecimiento = fechaFallecimiento;
    }

    public List<Libros> getLibros() {
        return libros;
    }

    public void setLibros(List<Libros> libros) {
        this.libros = libros;
    }

    public void addLibros(Libros libro) {
        this.libros.add(libro);
        libro.getAutores().add(this);
    }

    private String librosDeAutoresNombres (){
        return libros.stream().map(Libros::getTitulo).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return String.format("Autor: %s%n" +
                        "Fecha de nacimiento: %s%n" +
                        "Fecha de fallecimiento: %s%n" +
                        "libros: %s%n",
                nombre, fechaNacimiento, fechaFallecimiento, librosDeAutoresNombres());
    }
}
