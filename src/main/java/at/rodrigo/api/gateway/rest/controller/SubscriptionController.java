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

package at.rodrigo.api.gateway.rest.controller;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.rest.client.CapiClient;
import at.rodrigo.api.gateway.rest.repository.ApiRepository;
import at.rodrigo.api.gateway.rest.repository.CapiClientRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/subscription")
@io.swagger.annotations.Api(value = "Subscription Management", tags = {"Subscription Management"}, description = "Use these services to manage your subscriptions to API's")
@Slf4j
public class SubscriptionController {

    @Autowired
    CapiClientRepository capiClientRepository;

    @Autowired
    ApiRepository apiRepository;

    @ApiOperation(value = "Get all API's subcribed by a Client")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All API's subcribed")
    })
    @GetMapping(path = "/{clientId}")
    public ResponseEntity<List<Api>> getApisSubscribedByClient(@PathVariable String clientId) {

        List<Api> subscribedApis = new ArrayList<>();

        CapiClient capiClient = capiClientRepository.findByClientId(clientId);
        if(capiClient == null) {
            return new ResponseEntity<>(subscribedApis, HttpStatus.NOT_FOUND);
        }
        for(GrantedAuthority grantedAuthority : capiClient.getAuthorities()) {
            Optional<Api> api = apiRepository.findById(grantedAuthority.getAuthority());
            if(api.isPresent()) {
                subscribedApis.add(api.get());
            }
        }
        return new ResponseEntity<>(subscribedApis, HttpStatus.OK);
    }

    @ApiOperation(value = "Subscribe a client to an API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Updated Client")
    })
    @PostMapping(path = "/{clientId}/{apiId}")
    public ResponseEntity<CapiClient> subscribeApi(@PathVariable String clientId, @PathVariable String apiId) {

        CapiClient capiClient = capiClientRepository.findByClientId(clientId);
        Optional<Api> api = apiRepository.findById(apiId);
        if(capiClient == null || !api.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(api.get().getId());
        capiClient.getAuthorities().add(grantedAuthority);
        capiClientRepository.save(capiClient);
        return new ResponseEntity<>(capiClient, HttpStatus.OK);
    }

    @ApiOperation(value = "Unsubscribe a client to an API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Updated Client")
    })
    @DeleteMapping(path = "/{clientId}/{apiId}")
    public ResponseEntity<CapiClient> unsubscribeApi(@PathVariable String clientId, @PathVariable String apiId) {

        CapiClient capiClient = capiClientRepository.findByClientId(clientId);
        Optional<Api> api = apiRepository.findById(apiId);
        if(capiClient == null || !api.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(api.get().getId());
        capiClient.getAuthorities().remove(grantedAuthority);
        capiClientRepository.save(capiClient);
        return new ResponseEntity<>(capiClient, HttpStatus.OK);
    }
}
