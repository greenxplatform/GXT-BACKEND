package com.gxt.identity.dal;

import com.gxt.identity.entity.RevokedToken;
import java.util.Optional;

public interface RevokedTokenDal {
    RevokedToken save(RevokedToken revokedToken);

    boolean existsByTokenHash(String tokenHash);

    Optional<RevokedToken> findByTokenHash(String tokenHash);
}
