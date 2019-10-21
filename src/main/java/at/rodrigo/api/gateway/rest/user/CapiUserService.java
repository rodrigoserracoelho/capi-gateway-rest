package at.rodrigo.api.gateway.rest.user;

import at.rodrigo.api.gateway.rest.repository.CapiUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
public class CapiUserService implements UserDetailsService {

    @Autowired
    private CapiUserRepository capiUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CapiUser user = capiUserRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("Username %s not found", username));
        }
        String[] roles = new String[user.getRoles().size()];
        return new User(user.getUsername(), user.getPassword(), AuthorityUtils.createAuthorityList(user.getRoles().toArray(roles)));
    }
}