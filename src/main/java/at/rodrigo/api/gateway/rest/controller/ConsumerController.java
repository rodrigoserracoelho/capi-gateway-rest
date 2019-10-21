package at.rodrigo.api.gateway.rest.controller;

import at.rodrigo.api.gateway.key.entity.Consumer;
import at.rodrigo.api.gateway.rest.repository.ConsumerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/consumer")
@Slf4j
public class ConsumerController {

    private static final String FILTERED_CLIENT_ID_CHARS = "[^a-z0-9_\\x2D]";

    @Autowired
    ConsumerRepository consumerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    private ResponseEntity<Consumer> create(@RequestBody Consumer consumer, HttpServletRequest request) {

        if(consumerRepository.findById(sanitizeClientName(consumer.getClientName())).isPresent()) {
            return new ResponseEntity<>(consumer, HttpStatus.PRECONDITION_FAILED);
        }
        String consumerSecret = UUID.randomUUID().toString();
        consumer.setClientId(sanitizeClientName(consumer.getClientName()));
        consumer.setClientSecretHash(passwordEncoder.encode(consumerSecret));
        consumer.setClientSecret(consumerSecret);
        consumerRepository.save(consumer);
        return new ResponseEntity<>(consumer, HttpStatus.OK);
    }

    @GetMapping("/{id}/{secret}")
    private ResponseEntity<Consumer> find(@PathVariable String id, @PathVariable String secret, HttpServletRequest request) {
        Optional<Consumer> consumer = consumerRepository.findById(id);
        if(consumer.isPresent()) {
            if(passwordEncoder.matches(secret, consumer.get().getClientSecretHash())) {
                log.info("good password");
            } else {
                log.info("bad password");
            }
        }

        return new ResponseEntity<>(consumer.get(), HttpStatus.OK);
    }

    @GetMapping
    private ResponseEntity<Consumer> find(HttpServletRequest request) {
        return new ResponseEntity<>(new Consumer(), HttpStatus.OK);
    }

    protected String sanitizeClientName(String name) {
        return name.toLowerCase().replaceAll(" ", "-").replaceAll(FILTERED_CLIENT_ID_CHARS, "");
    }


}
