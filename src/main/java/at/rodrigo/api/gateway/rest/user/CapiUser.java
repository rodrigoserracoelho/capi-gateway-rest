package at.rodrigo.api.gateway.rest.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "user")
@Data
public class CapiUser {

    @Id
    private String id;

    private String username;
    private String password;
    private Set<String> roles;

}