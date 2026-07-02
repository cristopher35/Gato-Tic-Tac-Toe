package com.tuapp.gatoserver.repository;

import com.tuapp.gatoserver.model.RegisteredClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisteredClientRepository extends JpaRepository<RegisteredClient, Long> {

    Optional<RegisteredClient> findByClientId(String clientId);

    boolean existsByClientId(String clientId);
}
