package com.enigma.creditscoringapi.repository;

import com.enigma.creditscoringapi.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    Page<Customer> findAllContract(Pageable pageable);

    Page<Customer> findAllNon(Pageable pageable);

    Page<Customer> findAllRegular(Pageable pageable);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE customer u SET u.is_deleted = true WHERE u.id = ?1")
    void softDelete(String id);

    @Query(nativeQuery = true, value = "SELECT * FROM customer WHERE customer.submitter = :username")
    Page<Customer> findAllBySubmitter(String username, Pageable pageable);
}
