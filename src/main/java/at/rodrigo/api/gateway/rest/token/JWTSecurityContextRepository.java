/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package at.rodrigo.api.gateway.rest.token;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JWTSecurityContextRepository implements SecurityContextRepository {

    JWSChecker jwsChecker;
    JWKSet jwkSet;

    public JWTSecurityContextRepository(JWSChecker jwsChecker, JWKSet jwkSet) {
        this.jwsChecker = jwsChecker;
        this.jwkSet = jwkSet;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder httpRequestResponseHolder) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String token = tokenFromRequest(httpRequestResponseHolder.getRequest());
        try {
            if(token != null) {
                ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
                JWKSource keySource = new ImmutableJWKSet(jwkSet);
                JWSAlgorithm expectedJWSAlg = jwsChecker.getAlgorithm(token);
                JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
                jwtProcessor.setJWSKeySelector(keySelector);
                JWTClaimsSet claimsSet = jwtProcessor.process(token, null);
                if(claimsSet != null) {
                    String clientId = (String) claimsSet.getClaim("client_id");
                    List<String> authorities = (List<String>) claimsSet.getClaim("authorities");
                    User user = new User(clientId, "", getAuthorities(authorities));
                    context.setAuthentication(new TokenAuthentication(user));
                }
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }

        return context;
    }

    @Override
    public void saveContext(SecurityContext securityContext, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {}

    @Override
    public boolean containsContext(HttpServletRequest httpServletRequest) {
        return false;
    }

    private String tokenFromRequest(HttpServletRequest request) {
        String value = request.getHeader("Authorization");
        if (value != null && value.toLowerCase().startsWith("bearer")) {
            String[] parts = value.split(" ");
            return parts.length < 2 ? null : parts[1].trim();
        } else {
            return null;
        }
    }

    private List<GrantedAuthority> getAuthorities(List<String> authorities) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for(String authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }
        return grantedAuthorities;
    }
}
