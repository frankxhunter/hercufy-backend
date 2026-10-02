package com.hercufy.repositories;

import com.hercufy.models.RefreshToken;
import com.hercufy.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenId(String tokenId);

    void deleteByUser(User user);
}
