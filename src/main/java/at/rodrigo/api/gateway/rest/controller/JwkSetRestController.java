package at.rodrigo.api.gateway.rest.controller;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@io.swagger.annotations.Api(value = "CAPI Gateway Public JWK Endpoint", tags = {"CAPI Gateway Public JWK Endpoint"}, description = "If you are using CAPI Tokens to authorize third party servers.")
public class JwkSetRestController {

    @Autowired
    private JWKSet jwkSet;

    @GetMapping("${gateway.wellknown.jwk}")
    public Map<String, Object> keys() {
        return this.jwkSet.toJSONObject();
    }
}