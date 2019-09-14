package at.rodrigo.api.gateway.rest.controller;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.Verb;
import at.rodrigo.api.gateway.rest.repository.ApiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/route")
@Slf4j
public class RouteController {

    @Autowired
    ApiRepository apiRepository;

    @RequestMapping( path="/rest", method= RequestMethod.GET)
    public ResponseEntity<List<Api>> getRestRoutes(HttpServletRequest request) {
        return new ResponseEntity<>(apiRepository.findAll(), HttpStatus.OK);
    }


    @RequestMapping( path="/rest", method= RequestMethod.POST)
    public ResponseEntity<String> tempPost(HttpServletRequest request) {

        Api api1 = new Api();
        api1.setEndpoint("localhost:9010");
        api1.setName("ROD-SUPER-SAFE-API");
        api1.setSecured(true);
        api1.setContext("super-safe");
        api1.setJwsEndpoint("https://rodrigocoelho.auth0.com/.well-known/jwks.json");
        Path v1 = new Path();
        v1.setPath("/exposed");
        v1.setVerb(Verb.GET);
        Path v11 = new Path();
        v11.setPath("/exposed");
        v11.setVerb(Verb.POST);
        Path v12 = new Path();
        v12.setPath("/exposed");
        v12.setVerb(Verb.PUT);
        List<Path> api1PathList = new ArrayList<Path>();
        api1PathList.add(v1);
        api1PathList.add(v11);
        api1PathList.add(v12);
        api1.setPaths(api1PathList);

        apiRepository.save(api1);


        Api api2 = new Api();
        api2.setEndpoint("localhost:9010");
        api2.setName("ROD-UNSAFE-API");
        api2.setSecured(false);
        api2.setContext("super-unsafe");

        Path v2 = new Path();
        v2.setPath("/internal");
        v2.setVerb(Verb.GET);
        List<Path> api2PathList = new ArrayList<Path>();
        api2PathList.add(v2);
        api2.setPaths(api2PathList);

        List<Api> apiList = new ArrayList<Api>();
        apiList.add(api1);
        apiList.add(api2);

        apiRepository.save(api2);

        return new ResponseEntity<>(HttpStatus.OK);
    }


}
