package at.rodrigo.api.gateway.rest.repository;

import at.rodrigo.api.gateway.key.entity.Consumer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConsumerRepository extends MongoRepository<Consumer, String> {

}
