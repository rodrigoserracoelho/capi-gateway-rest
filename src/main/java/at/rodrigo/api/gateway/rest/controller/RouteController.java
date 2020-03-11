package at.rodrigo.api.gateway.rest.controller;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.rest.client.CapiClient;
import at.rodrigo.api.gateway.rest.configuration.CacheConstants;
import at.rodrigo.api.gateway.rest.repository.ApiRepository;
import at.rodrigo.api.gateway.rest.repository.CapiClientRepository;
import at.rodrigo.api.gateway.rest.validator.ApiValidator;
import com.hazelcast.core.HazelcastInstance;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/route")
@Slf4j
@io.swagger.annotations.Api(value = "API's Management", tags = {"API's Management"}, description = "Use these services to manage your API's")
public class RouteController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiValidator apiValidator;

    @Autowired
    private CapiClientRepository capiClientRepository;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    /*@ApiOperation(value = "Get all simple API's")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "API Info", response = Api.class),
            @ApiResponse(code = 400, message = "Bad request")
    })
    @GetMapping( path="/simple-rest" )
    public ResponseEntity<List<Api>> getSimpleRestRoutes(HttpServletRequest request) {
        return new ResponseEntity<>(apiRepository.findAllBySwagger(false), HttpStatus.OK);
    }*/

    @ApiOperation(value = "Get all API's (swagger)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "API Info", response = Api.class),
            @ApiResponse(code = 400, message = "Bad request")
    })
    @GetMapping( path="/swagger-rest" )
    public ResponseEntity<List<Api>> getSwaggerRestRoutes(HttpServletRequest request) {
        return new ResponseEntity<>(apiRepository.findAllBySwagger(true), HttpStatus.OK);
    }

    @ApiOperation(value = "Publish an API with an exposed swagger endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "API Created"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 412, message = "Pre Condition failed")
    })
    @PostMapping( path="/swagger-rest" )
    public ResponseEntity<String> postSwaggerEndpoints(@RequestBody Api api, HttpServletRequest request) {
        if(apiValidator.isApiValid(api)) {
            apiRepository.save(api);
            hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME).put(api.getId(), api);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        /*api.setId(UUID.randomUUID().toString());
        apiRepository.save(api);
        hazelcastInstance.getMap(CacheConstants.RUNNING_API_IMAP_NAME).put(api.getId(), api);
        return new ResponseEntity<>(HttpStatus.OK);*/
    }

    /*@ApiOperation(value = "Publish an API with your custom defined path")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "API Created"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 412, message = "Pre Condition failed")
    })
    @PostMapping( path="/simple-rest" )
    public ResponseEntity<Api> postSimpleRestEndpoints(@RequestBody Api api, HttpServletRequest request) {
        if(apiValidator.isApiValid(api)) {
            apiRepository.save(api);
            hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME).put(api.getId(), api);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }*/

    @ApiOperation(value = "Delete an API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "API Deleted"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 412, message = "Pre Condition failed")
    })
    @DeleteMapping( path="/{apiId}" )
    public ResponseEntity<String> deleteApi(@PathVariable String apiId, HttpServletRequest request) {
        boolean canDelete = true;
        Optional<Api> api = apiRepository.findById(apiId);
        if(api.isPresent()) {
            List<CapiClient> clients = capiClientRepository.findAll();
            for(CapiClient capiClient : clients) {
                Collection<GrantedAuthority> grantedAuthorities = capiClient.getAuthorities();
                for(GrantedAuthority grantedAuthority : grantedAuthorities) {
                    if(grantedAuthority.getAuthority().equals(api.get().getId())) {
                        canDelete = false;
                        break;
                    }
                }
            }
        }
        if(canDelete) {
            apiRepository.delete(api.get());
            hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME).remove(api.get().getId());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("API Contains active subscribers", HttpStatus.FORBIDDEN);
    }


}
