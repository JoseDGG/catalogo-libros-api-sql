package com.jdgg.catalogo_libros.principal;

import com.jdgg.catalogo_libros.modelos.*;
import com.jdgg.catalogo_libros.repositorio.AutoresRepository;
import com.jdgg.catalogo_libros.repositorio.LibrosRepository;
import com.jdgg.catalogo_libros.service.ConsumoAPI;
import com.jdgg.catalogo_libros.service.ConvierteDatos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
                6.) - Estadísticas sobre descargas de libros
                7.) - Top 10 libros más famosos
                8.) - Buscar por autor
                0.) - salir de la aplicación
                *********************************************
                """);
    }

    private void procesarOpcionMenu(){
        //Selecciona una opción entre 1 y 5
        switch (eleccionUsuario){
            //Base de datos y API
            case 1 -> buscarLibroPorTitulo();
            //Secreto. Ideado de forma alternativa para guardar varios libros de un mismo autor. API
            case 1221 -> guardarMultiplesLibros1SoloAutor();
            case 2 -> verLibrosRegistrados();
            case 3 -> verAutoresRegistrados();
            case 4 -> verAutoresVivosPorAno();
            case 5 -> listarLibrosPorIdioma();
            case 6 -> estadisticasDeLibros();
            //API
            case 7 -> top10Libros();
            case 8 -> buscarPorAutor();
            case 0 -> System.out.println("saliendo del programa...");
            default -> System.out.println("Por favor escriba una opción del menú -->");
        }
    }

    //MÉTODOS DE SERVICIO//

    //Petición a API y convertir json a clase GeneralDatos.
    private GeneralDatos llamarAPI(String tituloBuscar){
        System.out.println("buscando en la web...");
        var json = consumoAPI.obtenerDatos(baseURL + "?search=" + tituloBuscar.replace(" ", "+"));
        if (json != null){
            return convierteDatos.obtenerDatos(json, GeneralDatos.class);
        }else {
            return null;
        }
    }

    //Crea la entidad libro, para cada libro se le agrega una lista de autores.
    private List<Libros> crearLibrosYAutores(GeneralDatos generalDatos, int limite){
        return generalDatos.resultados().stream()
                //Limita el número de resultados
                .limit(limite)
                .map(libro -> {
                    //Crea lista de autores para cada libro
                    List<Autores> autoresLibro = libro.autores().stream()
                            .map(autor -> new Autores(
                                    autor.autor(),
                                    autor.fechaNacimiento(),
                                    autor.fechaFallecimiento()
                            ))
                            .toList();
                    //Crea cada libro que pasa por el stream y agrega la relación bidireccional (libros/autores).
                    Libros libroNuevo = new Libros(libro.titulo(),libro.idiomas(), libro.descargas());
                    libroNuevo.addAutorList(autoresLibro);
                    return libroNuevo;
                })
                //Agrega libro por libro a la lista de libros
                .toList();
    }

    private String readInputStringWithoutBlank(String mensaje){
        System.out.println(mensaje);
        String inputText = input.nextLine();
        while(inputText.isBlank()){
            inputText = input.nextLine();
        }
        return inputText;
    }

    //MÉTODOS DE BÚSQUEDA DEL MENÚ//

    private void buscarLibroPorTitulo() {
        String tituloBuscar = readInputStringWithoutBlank("Ingrese el titulo del libro que desea buscar:");

        //Revisamos si el libro existe en nuestra base de datos.
        Optional<Libros> libroExiste = libroRepositorio.findByTituloContainingIgnoreCase(tituloBuscar);

        //Si existe en la base de datos lo mostramos en pantalla, caso contrario se solicita a la API
        if (libroExiste.isPresent()){
            System.out.println("Buscando en la base de datos...");
            System.out.println(libroExiste.get());
        }else {
            var datos = llamarAPI(tituloBuscar);

            //En caso de que el libro exista y se encuentre en la api se obtienen los resultados, caso contrario se hace saber.
            if(datos != null){
                System.out.println("El libro no se encontró en la base de datos, buscando en la web...");

                //Creamos las entidades del libro y sus autores, extraemos solo el primer resultado.
                List<Libros> libro = crearLibrosYAutores(datos, 1);
                //Guardamos en la base de datos, gracias a cascade se guardan las 2 entidades.
                libroRepositorio.save(libro.get(0));
                System.out.println(libro.get(0));
            }else {
                System.out.println("\nLibro no encontrado\n");
            }
        }
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

    private void estadisticasDeLibros() {
        List<Libros> libros = libroRepositorio.findAll();
        DoubleSummaryStatistics estadisticas = libros.stream()
                .filter(libro -> libro.getDescargas() > 0)
                .collect(Collectors.summarizingDouble(Libros::getDescargas));
        System.out.println("Se ha descargado un total de " + estadisticas.getMax() + " veces el libro mas descargado");
        System.out.println("La media de los libros descargados es: " + estadisticas.getAverage() + " descargas");
        System.out.println("Se han descargado libros un total de: " + estadisticas.getSum() + " veces");
    }

    private void top10Libros() {
        System.out.println("Buscando en la web...");
        var json = consumoAPI.obtenerDatos(baseURL);
        GeneralDatos generalDatos = convierteDatos.obtenerDatos(json, GeneralDatos.class);
        List<Libros> libros = crearLibrosYAutores(generalDatos, 10);
        libros.forEach(System.out::println);
    }

    private void buscarPorAutor() {
        String nombreBuscar = readInputStringWithoutBlank("Ingrese el nombre del autor:");
        Optional<Autores> autor = autoresRepositorio.findByNombreContainingIgnoreCase(nombreBuscar);
        if (autor.isPresent()){
            Autores autorBuscado = autor.get();
            System.out.println(autor.get());
            System.out.println("¿Desea ver los detalles de los libros?");
            System.out.println("Presione: 1 - SI / cualquier tecla - NO");
            String mostrarLibros = input.nextLine();
            if (mostrarLibros == "1"){
                List<Libros> libros = autorBuscado.getLibros();
                libros.forEach(System.out::println);
            }else {
                System.out.println("\n");
            }
        }else {
            System.out.println("Autor no encontrado");
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
            String tituloBuscar = readInputStringWithoutBlank("Ingrese el titulo del libro que desea buscar:");
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
