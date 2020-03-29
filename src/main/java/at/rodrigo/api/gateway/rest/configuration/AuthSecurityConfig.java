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

package at.rodrigo.api.gateway.rest.configuration;

import at.rodrigo.api.gateway.rest.token.CAPIToken;
import at.rodrigo.api.gateway.rest.token.JWSChecker;
import at.rodrigo.api.gateway.rest.token.JWTSecurityContextRepository;
import at.rodrigo.api.gateway.rest.user.CapiUserService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class AuthSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${server.ssl.key-alias}")
    String serverSslKeyAlias;

    @Value("${server.ssl.key-store}")
    String serverSslKeyStore;

    @Value("${gateway.token.endpoint}")
    String oauthEndpoint;

    @Value("${gateway.wellknown.jwk}")
    String wellKnownJwk;

    @Autowired
    JWSChecker jwsChecker;

    @Override
    @Bean
    protected UserDetailsService userDetailsService() {
        return new CapiUserService();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Bean
    KeyStoreKeyFactory keyStoreKeyFactory() {
        return new KeyStoreKeyFactory(new FileSystemResource(serverSslKeyStore), serverSslKeyAlias.toCharArray());
    }

    @Bean
    public JWKSet jwkSet() {
        KeyPair keyPair = keyStoreKeyFactory().getKeyPair(serverSslKeyAlias);
        RSAKey.Builder builder = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(serverSslKeyAlias);
        return new JWKSet(builder.build());
    }

    @Bean
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }


    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        CAPIToken converter = new CAPIToken();
        converter.setKeyPair(keyStoreKeyFactory().getKeyPair(serverSslKeyAlias));
        return converter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .securityContext()
            .securityContextRepository(new JWTSecurityContextRepository(jwsChecker, jwkSet()))
            .and()
            .exceptionHandling()
            .and()
            .csrf()
            .ignoringAntMatchers(oauthEndpoint)
            .disable()
            .authorizeRequests().antMatchers(wellKnownJwk,
                "/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**", "/swagger/**").anonymous()
            .anyRequest().authenticated();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}