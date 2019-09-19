package at.rodrigo.api.gateway.rest.controller;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.EndpointType;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.Verb;
import at.rodrigo.api.gateway.rest.repository.ApiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/route")
@Slf4j
public class RouteController {

    @Autowired
    ApiRepository apiRepository;

    @GetMapping( path="/simple-rest" )
    public ResponseEntity<List<Api>> getSimpleRestRoutes(HttpServletRequest request) {
        return new ResponseEntity<>(apiRepository.findAllBySwagger(false), HttpStatus.OK);
    }

    @GetMapping( path="/swagger-rest" )
    public ResponseEntity<List<Api>> getSwaggerRestRoutes(HttpServletRequest request) {
        return new ResponseEntity<>(apiRepository.findAllBySwagger(true), HttpStatus.OK);
    }

    @PostMapping( path="/swagger-rest" )
    public ResponseEntity<String> postSwaggerEndpoints(HttpServletRequest request) {

        /*Api api1 = new Api();
        api1.setEndpoint("localhost:9010");
        api1.setEndpointType(EndpointType.HTTP);
        api1.setName("ROD-SUPER-SAFE-API");
        api1.setSecured(true);
        api1.setContext("super-safe");
        api1.setJwsEndpoint("https://rodrigocoelho.auth0.com/.well-known/jwks.json");
        api1.setId(UUID.randomUUID().toString());
        Path v1 = new Path();
        v1.setPath("/exposed");
        v1.setVerb(Verb.GET);
        v1.setBlockIfInError(false);
        v1.setMaxAllowedFailedCalls(-1);
        Path v11 = new Path();
        v11.setPath("/exposed");
        v11.setVerb(Verb.POST);
        v11.setBlockIfInError(false);
        v11.setMaxAllowedFailedCalls(-1);
        Path v12 = new Path();
        v12.setPath("/exposed");
        v12.setVerb(Verb.PUT);
        v12.setBlockIfInError(true);
        v12.setMaxAllowedFailedCalls(5);
        List<Path> api1PathList = new ArrayList<Path>();
        api1PathList.add(v1);
        api1PathList.add(v11);
        api1PathList.add(v12);
        api1.setPaths(api1PathList);

        apiRepository.save(api1);*/

        Api api2 = new Api();
        api2.setEndpoint("localhost:9010");
        api2.setEndpointType(EndpointType.HTTP);
        api2.setName("ROD-UNSAFE-API");
        api2.setSecured(false);
        api2.setSwagger(true);
        api2.setSwaggerEndpoint("http://localhost:9010/v2/api-docs");

        api2.setContext("super-unsafe");
        api2.setId(UUID.randomUUID().toString());

        apiRepository.save(api2);

        List<Api> apiList = new ArrayList<>();
        apiList.add(api2);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping( path="/simple-rest" )
    public ResponseEntity<String> postSimpleRestEndpoints(HttpServletRequest request) {

        Api api3 = new Api();
        api3.setEndpoint("gateway.theinterlink.eu:9443");
        api3.setEndpointType(EndpointType.HTTPS);
        api3.setName("WSO2-HEALTH-ENDPOINT");
        api3.setSecured(false);
        api3.setContext("wso2");
        api3.setId(UUID.randomUUID().toString());
        api3.setSwagger(false);

        Path v3 = new Path();
        v3.setPath("/services/Version");
        v3.setVerb(Verb.GET);
        v3.setBlockIfInError(false);
        v3.setMaxAllowedFailedCalls(-1);
        List<Path> api3PathList = new ArrayList<Path>();
        api3PathList.add(v3);
        api3.setPaths(api3PathList);


        apiRepository.save(api3);

        List<Api> apiList = new ArrayList<>();
        apiList.add(api3);

        return new ResponseEntity<>(HttpStatus.OK);
    }


}
