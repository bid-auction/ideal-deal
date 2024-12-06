package com.auction.bid.domain.product.repository;

import com.auction.bid.domain.photo.Photo;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
