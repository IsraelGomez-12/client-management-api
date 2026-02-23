package com.clientmanagement.repository;

import com.clientmanagement.entity.Client;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClientRepository implements PanacheRepository<Client> {

    public List<Client> findByCountryCode(String countryCode) {
        return list("countryCode = ?1 and active = true", countryCode.toUpperCase());
    }

    public Optional<Client> findActiveByUuid(String uuid) {
        return find("uuid = ?1 and active = true", uuid).firstResultOptional();
    }

    public Optional<Client> findByEmail(String email) {
        return find("email = ?1 and active = true", email.toLowerCase()).firstResultOptional();
    }

    public boolean existsByEmail(String email) {
        return count("email = ?1 and active = true", email.toLowerCase()) > 0;
    }

    public boolean existsByEmailAndUuidNot(String email, String excludeUuid) {
        return count("email = ?1 and uuid != ?2 and active = true", email.toLowerCase(), excludeUuid) > 0;
    }

    public boolean existsByPhone(String phone) {
        return count("phone = ?1 and active = true", phone) > 0;
    }

    public boolean existsByPhoneAndUuidNot(String phone, String excludeUuid) {
        return count("phone = ?1 and uuid != ?2 and active = true", phone, excludeUuid) > 0;
    }

    public List<Client> findAllOrderByCreatedAtDesc() {
        return list("active = true ORDER BY createdAt DESC");
    }

    public long countActive() {
        return count("active = true");
    }

    public long countActiveByCountryCode(String countryCode) {
        return count("countryCode = ?1 and active = true", countryCode.toUpperCase());
    }
}
