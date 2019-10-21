package at.rodrigo.api.gateway.rest.controller;

import at.rodrigo.api.gateway.cache.CacheConstants;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.rest.repository.ApiRepository;
import com.hazelcast.core.HazelcastInstance;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/route")
@Slf4j
@io.swagger.annotations.Api(value = "CAPI Route Management", tags = {"CAPI Route Management"}, description = "Use these services to manage your routes")
public class RouteController {

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @ApiOperation(value = "Get all custom routes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Route Info", response = Api.class),
            @ApiResponse(code = 400, message = "Bad request")
    })
    @GetMapping( path="/simple-rest" )
    public ResponseEntity<List<Api>> getSimpleRestRoutes(HttpServletRequest request) {
        return new ResponseEntity<>(apiRepository.findAllBySwagger(false), HttpStatus.OK);
    }

    @ApiOperation(value = "Get all Routes (swagger)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Route Info", response = Api.class),
            @ApiResponse(code = 400, message = "Bad request")
    })
    @GetMapping( path="/swagger-rest" )
    public ResponseEntity<List<Api>> getSwaggerRestRoutes(HttpServletRequest request) {
        return new ResponseEntity<>(apiRepository.findAllBySwagger(true), HttpStatus.OK);
    }

    @PostMapping( path="/swagger-rest" )
    public ResponseEntity<String> postSwaggerEndpoints(@RequestBody Api api, HttpServletRequest request) {
        if(apiRepository.findByName(api.getName()) != null) {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }
        api.setId(UUID.randomUUID().toString());
        apiRepository.save(api);
        hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME).put(api.getId(), api);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Publish REST API to WSO2")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Route Created"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 412, message = "Pre Condition failed")
    })
    @PostMapping( path="/simple-rest" )
    public ResponseEntity<Api> postSimpleRestEndpoints(@RequestBody Api api, HttpServletRequest request) {
        if(apiRepository.findByName(api.getName()) != null) {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }
        api.setId(UUID.randomUUID().toString());
        apiRepository.save(api);
        hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME).put(api.getId(), api);
        return new ResponseEntity<>(HttpStatus.OK);
    }



}
