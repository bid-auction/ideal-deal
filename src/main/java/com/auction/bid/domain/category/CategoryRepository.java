package com.auction.bid.domain.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> save(String categoryName);

    Optional<Category> findByCategoryName(String categoryName);
}
