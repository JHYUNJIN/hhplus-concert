package kr.hhplus.be.server.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findById(String id);

    @Modifying
    @Query("UPDATE UserEntity u SET u.amount = u.amount + :amount WHERE u.id = :userId")
    int chargePoint(@Param("userId") String userId, @Param("amount") BigDecimal amount);
}