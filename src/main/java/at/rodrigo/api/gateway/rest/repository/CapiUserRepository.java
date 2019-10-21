package at.rodrigo.api.gateway.rest.repository;

import at.rodrigo.api.gateway.rest.user.CapiUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CapiUserRepository extends MongoRepository<CapiUser, String> {
    CapiUser findByUsername(String username);
}
