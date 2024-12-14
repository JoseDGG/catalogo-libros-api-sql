package com.jdgg.catalogo_libros.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class ConsumoAPI {
    public String obtenerDatos(String url){
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .build();
//        System.out.println(request);
        HttpResponse<String> response = null;
        try {
            response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e){
            System.out.println("El tiempo de espera de la API ha sido superado");
            return null;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        //        System.out.println(response);
        return response.body();
    }
}
