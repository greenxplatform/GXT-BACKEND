package com.gxt.identity.dal.impl;

import com.gxt.identity.dal.RevokedTokenDal;
import com.gxt.identity.entity.RevokedToken;
import com.gxt.identity.repository.RevokedTokenRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevokedTokenDalImpl implements RevokedTokenDal {

    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    public RevokedToken save(RevokedToken revokedToken) {
        return revokedTokenRepository.save(revokedToken);
    }

    @Override
    public boolean existsByTokenHash(String tokenHash) {
        return revokedTokenRepository.existsByTokenHash(tokenHash);
    }

    @Override
    public Optional<RevokedToken> findByTokenHash(String tokenHash) {
        return revokedTokenRepository.findByTokenHash(tokenHash);
    }
}
