package br.com.cantina.Cantina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CantinaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CantinaApplication.class, args);
	}

}
