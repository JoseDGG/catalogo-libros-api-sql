package com.jdgg.catalogo_libros.principal;

import com.jdgg.catalogo_libros.modelos.*;
import com.jdgg.catalogo_libros.repositorio.AutoresRepository;
import com.jdgg.catalogo_libros.repositorio.LibrosRepository;
import com.jdgg.catalogo_libros.service.ConsumoAPI;
import com.jdgg.catalogo_libros.service.ConvierteDatos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class Principal {
    private Scanner input = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos convierteDatos = new ConvierteDatos();
    private String baseURL = "https://gutendex.com/books/";
    private LibrosRepository libroRepositorio;
    private AutoresRepository autoresRepositorio;
    private Autores autorAMultiplesLibros;

    int eleccionUsuario = -1;

    @Autowired
    public Principal(LibrosRepository librosrepository, AutoresRepository autoresRepository) {
        this.libroRepositorio = librosrepository;
        this.autoresRepositorio = autoresRepository;
    }

    public void menu() {
        //bucle que inicia el programa.
        while (eleccionUsuario != 0) {
            //muestra con un sout el menu.
            mostrarMenu();
            //Verifica que la opción elegida sea un número entero
            try {
                eleccionUsuario = input.nextInt();
                input.nextLine();
                //Dependiendo la opcion elegida invoca un metodo
                procesarOpcionMenu();
            } catch (InputMismatchException e) {
                System.out.println("¡¡No es una opción valida!!");
                input.nextLine(); //Limpia la entrada para no entrar en bucle
            }
        }
    }

    private void mostrarMenu(){
        System.out.println("""
                *********************************************
                1.) - Buscar libro por titulo
                2.) - Ver libros registrados
                3.) - Ver autores registrados
                4.) - Ver autores vivos en un determinado año
                5.) - listar libros por idioma
                0.) - salir de la aplicación
                *********************************************
                """);
    }

    private void procesarOpcionMenu(){
        //Selecciona una opción entre 1 y 5
        switch (eleccionUsuario){
            case 1 -> buscarLibroPorTitulo();
            //Secreto. Ideado de forma alternativa para guardar varios libros de un mismo autor
            case 1221 -> guardarMultiplesLibros1SoloAutor();
            case 2 -> verLibrosRegistrados();
            case 3 -> verAutoresRegistrados();
            case 4 -> verAutoresVivosPorAno();
            case 5 -> listarLibrosPorIdioma();
            case 0 -> System.out.println("saliendo del programa...");
            default -> System.out.println("Por favor escriba una opción del menú -->");
        }
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Ingrese el titulo del libro que desea buscar:");
        String tituloBuscar = input.nextLine();
        while(tituloBuscar.isBlank()){
            tituloBuscar = input.nextLine();
        }

        //Revisamos si el libro existe en nuestra base de datos.
        Optional<Libros> libroExiste = libroRepositorio.findByTituloContainingIgnoreCase(tituloBuscar);

        //Si existe en la base de datos lo mostramos en pantalla, caso contrario se solicita a la API
        if (libroExiste.isPresent()){
            System.out.println(libroExiste.get());
        }else {
            var datos = llamarAPI(tituloBuscar);
            //En caso de que el libro exista y se encuentre en la api se obtienen los resultados, caso contrario se hace saber.
            if(!datos.resultados().isEmpty()){
                //Extraemos el primer resultado de la conversión, ya que es una lista lo contenido en datos, solo queremos el primer resultado de la api.
                LibrosDatos librosDatos = datos.resultados().get(0);
                Libros libro = new Libros(librosDatos.titulo(),librosDatos.idiomas(),librosDatos.descargas());

                //Desarrollamos autores
                Autores autor = new Autores(librosDatos.autores().get(0).autor(), librosDatos.autores().get(0).fechaNacimiento(), librosDatos.autores().get(0).fechaFallecimiento());
                libro.addAutor(autor);

                //Guardamos en la base de datos, gracias a cascade se guardan las 2 entidades.
                libroRepositorio.save(libro);

                System.out.println(libro);
            }else {
                System.out.println("\nLibro no encontrado\n");
            }
        }
    }

    //Petición a API y convertir json a clase GeneralDatos.
    private GeneralDatos llamarAPI(String tituloBuscar){
        System.out.println("buscando en la web...");
        var json = consumoAPI.obtenerDatos(baseURL + "?search=" + tituloBuscar.replace(" ", "+"));
        return convierteDatos.obtenerDatos(json, GeneralDatos.class);
    }

    private void verLibrosRegistrados() {
        libroRepositorio.findAll().forEach(System.out::println);
    }

    private void verAutoresRegistrados() {
        autoresRepositorio.findAll().forEach(System.out::println);
    }

    private void verAutoresVivosPorAno() {
        System.out.println("Ingrese el año que desee ver que autores estaban vivos");
        int ano = 0;
        boolean isValid = false;
        while (!isValid){
            try{
                ano = input.nextInt();
                if (ano < 0){
                    System.out.println("El número no puede ser negativo");
                }
                else {
                    List<Autores> autores = autoresRepositorio.encontrarAutoresEnDeterminadoAno(ano);
                    if (autores.isEmpty()){
                        System.out.println("No se encontraron autores");
                        isValid = true;
                    }else {
                        System.out.println("Autores vivos en el año " + ano + ":");
                        autores.forEach(System.out::println);
                        isValid = true;
                    }
                }
            }catch (InputMismatchException e){
                System.out.println("Por favor ingrese un número entero");
                input.nextLine();
            }
        }
    }

    private void listarLibrosPorIdioma() {
        System.out.println("Ingrese el idioma para buscar los libros");
        System.out.println("Idiomas disponibles:");
        for (Idiomas idioma :Idiomas.values()){
            System.out.printf("- %s (%s)%n", idioma.getIdiomaLegible(), idioma.name());
        }
        String idioma = input.nextLine();
        String idiomaEnum = Idiomas.fromString(idioma);
        if ("Idioma no encontrado".equals(idiomaEnum)){
            System.out.println(idiomaEnum);
        }else {
            System.out.println(idiomaEnum);
            List<Libros> libros = libroRepositorio.buscarLibrosPorIdioma(idiomaEnum.toLowerCase());
            System.out.println("Libros encontrados: " + libros.size() + "\n");
            libros.forEach(System.out::println);
        }
    }

    //Se debe conocer los libros del autor de antemano
    private void guardarMultiplesLibros1SoloAutor() {
        System.out.println("""
                HA INGRESADO A UN BUCLE
                PRESIONE 0 PARA SALIR DE LA BÚSQUEDA Y GUARDADO EN BUCLE
                EN ESTE PROGRAMA ÚNICAMENTE SE LE PEDIRÁ EL NOMBRE DEL LIBRO
                LOS LIBROS INGRESADOS DEBEN SER ÚNICAMENTE DE UN MISMO AUTOR
                """);

        int bucleBusqueda = 1;
        List<Libros> libros = new ArrayList<>();
        while (bucleBusqueda != 0){
            System.out.println("PRESIONE 0 PARA SALIR, 1 PARA CONTINUAR");
            bucleBusqueda = input.nextInt();
            if (bucleBusqueda == 0){
                break;
            }
            System.out.println("Ingrese el titulo del libro que desea buscar:");
            String tituloBuscar = input.nextLine();
            while(tituloBuscar.isBlank()) {
                tituloBuscar = input.nextLine();
            }
            Optional<Libros> libroExiste = libroRepositorio.findByTituloContainingIgnoreCase(tituloBuscar);
            if (libroExiste.isPresent()){
                System.out.println(libroExiste.get());
            }else {
                var datos = llamarAPI(tituloBuscar);
                if(!datos.resultados().isEmpty()){
                    LibrosDatos librosDatos = datos.resultados().get(0);
                    Libros libro = new Libros(librosDatos.titulo(),librosDatos.idiomas(),librosDatos.descargas());
                    libros.add(libro);
                    returnAutor(new Autores(librosDatos.autores().get(0).autor(), librosDatos.autores().get(0).fechaNacimiento(), librosDatos.autores().get(0).fechaFallecimiento()));

                }else {
                    System.out.println("\nLibro no encontrado\n");
                }
            }
        }
        //A cada libro se le agrega el autor para posterior guardar las nuevas entidades en la base de datos.
        libros.forEach(d-> d.addAutor(autorAMultiplesLibros));
        libros.forEach(d-> libroRepositorio.save(d));
        libros.forEach(System.out::println);
    }

    private void returnAutor(Autores autor){
        this.autorAMultiplesLibros = autor;
    }
}
