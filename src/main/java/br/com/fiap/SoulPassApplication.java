package br.com.fiap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da API SoulPass.
 * Ao rodar, sobe o servidor e procura os @RestController dentro de br.com.fiap.
 */
@SpringBootApplication
public class SoulPassApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoulPassApplication.class, args);
    }
}
