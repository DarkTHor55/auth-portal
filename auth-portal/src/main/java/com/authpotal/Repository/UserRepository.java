package com.authpotal.Repository;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

import com.authpotal.Entity.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User,Long>{
     public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
    
}
