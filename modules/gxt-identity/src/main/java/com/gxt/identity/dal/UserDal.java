package com.gxt.identity.dal;

import com.gxt.identity.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserDal {
    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByEmail(String email);
}
