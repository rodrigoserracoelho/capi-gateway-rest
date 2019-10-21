package at.rodrigo.api.gateway.rest.controller;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@io.swagger.annotations.Api(value = "CAPI JWK Endpoint", tags = {"CAPI JWK Endpoint"}, description = "")
public class JwkSetRestController {

    @Autowired
    private JWKSet jwkSet;

    @GetMapping("${gateway.wellknown.jwk}")
    public Map<String, Object> keys() {
        return this.jwkSet.toJSONObject();
    }
}