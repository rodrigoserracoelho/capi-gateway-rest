package at.rodrigo.api.gateway.rest;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@SpringBootApplication
@EnableSwagger2
public class ApiGateway {
    public static void main(String[] args) {
        SpringApplication.run(ApiGateway.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Docket labelApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .produces(new HashSet<>(Arrays.asList("application/json")))
                .consumes(new HashSet<>(Arrays.asList("application/json")))
                .select()
                .apis(RequestHandlerSelectors.basePackage("at.rodrigo.api.gateway.rest"))
                //.paths(paths())
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .paths(Predicates.not(PathSelectors.regex("/consumer.*")))
                .paths(Predicates.not(PathSelectors.regex("/grafana.*")))
                .build();
                //.securitySchemes(newArrayList(basicAuth()))
                //.securityContexts(newArrayList(securityContext()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("CAPI Gateway Management Endpoint")
                .description("Automation Endpoint for publishing on the API Gateway")
                .version("1.0").contact(new Contact("SURISOFT","","me@rodrigo.at"))
                .build();
    }

    private Predicate<String> paths() {
        Collection<String> paths = new ArrayList<>();
        paths.add("/rest.*");
        return Predicates.in(paths);
        //return Predicates.not(PathSelectors.regex("/basic-error-controller.*"));
    }
}
