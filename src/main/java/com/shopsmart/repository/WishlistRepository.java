package com.shopsmart.repository;

import com.shopsmart.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUsernameOrderByAddedAtDesc(String username);
    Optional<WishlistItem> findByUsernameAndProductId(String username, Long productId);

    @Modifying
    @Transactional
    void deleteByUsernameAndProductId(String username, Long productId);
}
