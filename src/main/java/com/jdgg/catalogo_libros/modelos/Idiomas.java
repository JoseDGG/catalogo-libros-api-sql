package com.jdgg.catalogo_libros.modelos;

public enum Idiomas {
    ES("Español"),
    EN("Ingles");

    private String idiomaLegible;

    Idiomas (String idioma){
        this.idiomaLegible = idioma;
    }

    public static String fromString(String texto){
        for (Idiomas idioma : Idiomas.values()){
            if(idioma.idiomaLegible.equalsIgnoreCase(texto)){
                return idioma.toString();
            }
        }
        return "Idioma no encontrado";
    }

    public static String idiomaLegible(String texto){
        for (Idiomas idioma : Idiomas.values()){
            if(texto.equalsIgnoreCase(idioma.name())){
                return idioma.idiomaLegible;
            }
        }
        return texto;
    }

    public String getIdiomaLegible() {
        return idiomaLegible;
    }
}
