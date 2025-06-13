package com.example.datnbe.Repository;

import com.example.datnbe.Entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, String> {

    @Query(value = """
                SELECT DATE_FORMAT(p.payment_date, '%Y-%m') AS month, SUM(p.amount) AS total_amount
                FROM payments p
                WHERE p.payment_date BETWEEN :startDate AND :endDate
                GROUP BY DATE_FORMAT(p.payment_date, '%Y-%m')
                ORDER BY DATE_FORMAT(p.payment_date, '%Y-%m')
            """, nativeQuery = true)
    List<Object[]> getStatisticByMonthNative(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
