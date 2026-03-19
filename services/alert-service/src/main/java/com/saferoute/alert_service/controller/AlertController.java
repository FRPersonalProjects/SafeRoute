package com.saferoute.alert_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saferoute.alert_service.dto.TelemetryDTO;
import com.saferoute.alert_service.service.AlertService;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    // receber os dados de telemetria e verificar se ha alertas
    @PostMapping("/check")
    public ResponseEntity<Void> checkTelemetry(@RequestBody TelemetryDTO data) {
        alertService.handleAlert(data);
        return ResponseEntity.ok().build();
    }
}