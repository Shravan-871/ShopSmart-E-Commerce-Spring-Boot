package com.shopsmart.repository;

import com.shopsmart.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUsernameOrderByAddedAtDesc(String username);
    Optional<WishlistItem> findByUsernameAndProductId(String username, Long productId);
    void deleteByUsernameAndProductId(String username, Long productId);
}
