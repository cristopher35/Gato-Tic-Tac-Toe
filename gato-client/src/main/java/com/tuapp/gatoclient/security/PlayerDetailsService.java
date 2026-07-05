package com.tuapp.gatoclient.security;

import com.tuapp.gatoclient.repository.PlayerRepository;
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
public class PlayerDetailsService implements UserDetailsService {

    private final PlayerRepository playerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return playerRepository.findByUsername(username)
            .map(player -> new User(
                player.getUsername(),
                player.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_PLAYER"))
            ))
            .orElseThrow(() -> new UsernameNotFoundException("Jugador no encontrado: " + username));
    }
}
