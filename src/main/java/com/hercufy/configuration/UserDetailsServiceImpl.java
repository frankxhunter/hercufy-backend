package com.hercufy.configuration;

import com.hercufy.models.User;
import com.hercufy.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Notar que aqui "username" (termino de Spring Security) se refiere al email:
        // es el identificador de acceso. El nombre visible del usuario vive aparte, en User.username.
        User user = userService.findByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException("User details not found for the user with email: " + username));
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().toString()));

        return new org.springframework.security.core.userdetails
                .User(user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                user.isEmailVerified(),
                true,
                true,
                true,
                authorities);
    }

}
