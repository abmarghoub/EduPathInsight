package ens.edupath.auth.repository;

import ens.edupath.auth.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByEmailAndCodeAndUsedFalse(String email, String code);
    
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.expiresAt < ?1")
    void deleteExpiredCodes(LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.email = ?1")
    void deleteByEmail(String email);
}


