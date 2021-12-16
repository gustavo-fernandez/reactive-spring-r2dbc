package com.example.reativespringr2dbc;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class PersonController {

  private final PersonRepository personRepository;

  @GetMapping("/api/person/{id}")
  public Mono<Person> getPerson(@PathVariable Long id) {
    return personRepository.findById(id);
  }

  @GetMapping("/api/person")
  public Flux<Person> getPeople() {
    return personRepository.findAll();
  }

  // SSE -> Server Sent Events
  @GetMapping(value = "api/person/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> uuids() {
    return Flux.fromStream(Stream.generate(() -> UUID.randomUUID().toString()))
      .delayElements(Duration.ofSeconds(1L));
  }

}
