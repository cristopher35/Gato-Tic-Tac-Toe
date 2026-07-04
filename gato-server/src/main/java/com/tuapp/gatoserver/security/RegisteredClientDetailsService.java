package com.tuapp.gatoserver.security;

import com.tuapp.gatoserver.repository.RegisteredClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisteredClientDetailsService implements UserDetailsService {

    private final RegisteredClientRepository registeredClientRepository;

    @Override
    public UserDetails loadUserByUsername(String clientId) throws UsernameNotFoundException {
        return registeredClientRepository.findByClientId(clientId)
            .map(client -> {
                log.info("Cliente autenticado: clientId={}", clientId);
                return new User(
                    client.getClientId(),
                    client.getClientSecret(),
                    List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
                );
            })
            .orElseThrow(() -> {
                log.warn("Cliente no registrado: clientId={}", clientId);
                return new UsernameNotFoundException("Cliente no registrado: " + clientId);
            });
    }
}
