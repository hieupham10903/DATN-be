package com.example.datnbe.Service;

import com.example.datnbe.Entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentsRepository extends JpaRepository<Payments, String> {
}