package at.rodrigo.api.gateway.rest.client;

import at.rodrigo.api.gateway.rest.repository.CapiClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

@Slf4j
public class CapiClientService implements ClientDetailsService {

    @Autowired
    CapiClientRepository capiClientRepository;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        CapiClient clientDetails = capiClientRepository.findByClientId(clientId);
        if (clientDetails == null) {
            throw new ClientRegistrationException(String.format("Client with id %s not found", clientId));
        }
        return clientDetails;
    }


}