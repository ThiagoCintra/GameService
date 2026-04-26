package com.gameservice.infrastructure.persistence;

import com.gameservice.domain.CustomerProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface CustomerProgressRepository extends JpaRepository<CustomerProgress, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CustomerProgress c where c.customerId = :customerId")
    Optional<CustomerProgress> findByCustomerIdForUpdate(@Param("customerId") String customerId);

    Optional<CustomerProgress> findByCustomerId(String customerId);
}
