package at.rodrigo.api.gateway.rest.repository;

import at.rodrigo.api.gateway.rest.client.CapiClient;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CapiClientRepository extends MongoRepository<CapiClient, String> {
    CapiClient findByClientId(String clientId);
}
