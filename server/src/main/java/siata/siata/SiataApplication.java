package siata.siata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SiataApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiataApplication.class, args);
	}

}