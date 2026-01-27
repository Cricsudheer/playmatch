package com.example.playmatch.mvp.users.repository;

import com.example.playmatch.mvp.users.model.MvpUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MvpUserRepository extends JpaRepository<MvpUser, Long> {
    Optional<MvpUser> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
}
