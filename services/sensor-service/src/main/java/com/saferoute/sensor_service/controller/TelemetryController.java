package com.saferoute.sensor_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saferoute.sensor_service.dto.TelemetryDTO;
import com.saferoute.sensor_service.service.TelemetryService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/telemetry")
public class TelemetryController {

    @Autowired
    private TelemetryService telemetryService;

    // CircuitBreaker para lidar com falhas no Alert-Service
    @PostMapping
    @CircuitBreaker(name = "alertServiceCB", fallbackMethod = "fallbackAlert")
    public ResponseEntity<String> receiveData(@RequestBody TelemetryDTO data) {
        System.out.println("Dados do caminhão " + data.getTruckId() + " recebidos.");
        telemetryService.processTelemetry(data);
        return ResponseEntity.ok("Processado com sucesso.");
    }

    // Se o Alert-Service cair, fallback assume
    public ResponseEntity<String> fallbackAlert(TelemetryDTO data, Throwable t) {
        // System.err.println("ALERTA: Serviço de análise fora do ar");
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Dados recebidos, análise em espera (Modo de Segurança)."); // pendente de análise, mas aceita os
                                                                                  // dados para não perder info
    }
}