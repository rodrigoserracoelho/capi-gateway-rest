package at.rodrigo.api.gateway.rest.repository;

import at.rodrigo.api.gateway.entity.Api;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiRepository extends MongoRepository<Api, String> {


}
