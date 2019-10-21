package at.rodrigo.api.gateway.rest.controller;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.key.entity.Consumer;
import at.rodrigo.api.gateway.rest.client.CapiClient;
import at.rodrigo.api.gateway.rest.user.CapiUser;
import at.rodrigo.api.gateway.rest.repository.ApiRepository;
import at.rodrigo.api.gateway.rest.repository.ConsumerRepository;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/consumer")
@Slf4j
public class ConsumerController {

    private static final String FILTERED_CLIENT_ID_CHARS = "[^a-z0-9_\\x2D]";

    @Autowired
    ConsumerRepository consumerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ApiRepository apiRepository;

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

        List<Api> apis = apiRepository.findAll();
        Set<String> routes = new HashSet<>();
        for(Api api : apis) {
            routes.add(api.getId());
        }

        // init the users
        CapiUser capiUser = new CapiUser();
        capiUser.setUsername("user");
        capiUser.setPassword(passwordEncoder.encode("user"));
        capiUser.setRoles(Sets.newHashSet(("ROLE_USER")));
        mongoTemplate.save(capiUser);

        // init the client details
        CapiClient clientDetails = new CapiClient();
        clientDetails.setClientId("web-client");
        clientDetails.setClientSecret(passwordEncoder.encode("web-client-secret"));
        clientDetails.setSecretRequired(true);

        clientDetails.setResourceIds(routes);
        clientDetails.setScope(Sets.newHashSet("read-foo"));
        clientDetails.setAuthorizedGrantTypes(Sets.newHashSet("authorization_code", "refresh_token",
                "password", "client_credentials"));
        clientDetails.setRegisteredRedirectUri(Sets.newHashSet("http://localhost:8082/resource-service"));
        clientDetails.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        clientDetails.setAccessTokenValiditySeconds(60);
        clientDetails.setRefreshTokenValiditySeconds(14400);
        clientDetails.setAutoApprove(false);
        mongoTemplate.save(clientDetails);

        CapiClient clientDetails2 = new CapiClient();
        clientDetails2.setClientId("web-publisher");
        clientDetails2.setClientSecret(passwordEncoder.encode("web-client-secret"));
        clientDetails2.setSecretRequired(true);

        clientDetails2.setResourceIds(routes);
        clientDetails2.setScope(Sets.newHashSet("read-foo"));
        clientDetails2.setAuthorizedGrantTypes(Sets.newHashSet("authorization_code", "refresh_token",
                "password", "client_credentials"));
        clientDetails2.setRegisteredRedirectUri(Sets.newHashSet("http://localhost:8082/resource-service"));
        clientDetails2.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_PUBLISHER"));
        clientDetails2.setAccessTokenValiditySeconds(60);
        clientDetails2.setRefreshTokenValiditySeconds(14400);
        clientDetails2.setAutoApprove(false);
        mongoTemplate.save(clientDetails2);

        return new ResponseEntity<>(new Consumer(), HttpStatus.OK);
    }

    protected String sanitizeClientName(String name) {
        return name.toLowerCase().replaceAll(" ", "-").replaceAll(FILTERED_CLIENT_ID_CHARS, "");
    }


}
