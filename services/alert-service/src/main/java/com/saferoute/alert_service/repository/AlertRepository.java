package com.saferoute.alert_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saferoute.alert_service.model.Alert;

public interface AlertRepository extends JpaRepository<Alert, Long> {

}
