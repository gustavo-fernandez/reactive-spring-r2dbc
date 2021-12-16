package com.example.reativespringr2dbc;

import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
public class ReactiveSpringR2dbcApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSpringR2dbcApplication.class, args);
	}

	// @Bean // With Spring Data
	CommandLineRunner cmd(PersonRepository personRepository) {
		return args -> {
			Mono<Person> crearAGustavo = personRepository.save(new Person(null, "Gustavo"));
			Mono<Person> crearAGiancarlo = personRepository.save(new Person(null, "Giancarlo"));
			Mono<Person> crearAPablo = personRepository.save(new Person(null, "Pablo"));
			Flux<Person> consultarATodos = personRepository.findAll();

			crearAGustavo
				.then(crearAGiancarlo)
				.then(crearAPablo)
				.thenMany(consultarATodos)
				.doOnNext(person -> log.info("Person: {}", person))
				.subscribe();
		};
	}

	@Bean // With Plain R2DBC
	CommandLineRunner cmd1(ConnectionFactory connectionFactory) {
		return args -> {
			R2dbcEntityTemplate template = new R2dbcEntityTemplate(connectionFactory);
			Mono<Person> jorge = template.insert(new Person(null, "Jorge"));
			Mono<Person> luis = template.insert(new Person(null, "Luis"));
			Mono<Person> tommy = template.insert(new Person(null, "Tommy"));

			Flux<String> consultarTodos = Mono.from(connectionFactory.create())
				.flatMapMany(connection -> connection.createStatement("SELECT name FROM person")
					.execute()
				)
				.flatMap(result -> result.map((row, rm) -> row.get("name", String.class)))
				.doOnNext(name -> log.info("Name: {}", name));

			jorge.then(luis).then(tommy).thenMany(consultarTodos).subscribe();
		};
	}

	// X --> map : Mono<Y> --> Publisher<Y>
	// X --> flatMap : Publisher<Y> --> Y

}
