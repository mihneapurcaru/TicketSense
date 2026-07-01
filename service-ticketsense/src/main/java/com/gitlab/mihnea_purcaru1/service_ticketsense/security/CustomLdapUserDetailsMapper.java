package com.gitlab.mihnea_purcaru1.service_ticketsense.security;

import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.Role;
import com.gitlab.mihnea_purcaru1.service_ticketsense.entity.User;
import com.gitlab.mihnea_purcaru1.service_ticketsense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomLdapUserDetailsMapper implements UserDetailsContextMapper {

    private final UserRepository userRepository;

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        User localUser = syncUserWithLocalDb(ctx, username);
        List<GrantedAuthority> grantedAuthorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + localUser.getRole().name())
        );
        return new org.springframework.security.core.userdetails.User(username, "", grantedAuthorities);
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
    }

    private User syncUserWithLocalDb(DirContextOperations ctx, String username) {
        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);

            String firstName = ctx.getStringAttribute("givenName");
            String lastName = ctx.getStringAttribute("sn");
            String email = ctx.getStringAttribute("mail");

            newUser.setFirstName(firstName != null ? firstName : username);
            newUser.setLastName(lastName != null ? lastName : "");
            newUser.setEmail(email != null ? email : username + "@mycompany.local");
            newUser.setRole(Role.NORMAL_USER);
            newUser.setIsActive(true);

            return userRepository.save(newUser);
        });

        String title = ctx.getStringAttribute("title");
        Role role = mapTitleToRole(title);
        if (user.getRole() != role) {
            user.setRole(role);
            user = userRepository.save(user);
        }
        return user;
    }

    private Role mapTitleToRole(String title) {
        if (title == null) return Role.NORMAL_USER;
        return switch (title.toLowerCase()) {
            case "ticketsense_admin" -> Role.ADMIN;
            case "ticketsense_it_support" -> Role.IT_SUPPORT_MEMBER;
            default -> Role.NORMAL_USER;
        };
    }
}
