package com.saferoute.alert_service.controller;

import com.saferoute.alert_service.dto.TelemetryDTO;
import com.saferoute.alert_service.service.AlertService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertController")
class AlertControllerTest {

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertController alertController;

    @Test
    @DisplayName("checkTelemetry deve retornar 200 OK")
    void checkTelemetry_deveRetornar200() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", 5.0, -23.55, -46.63, null);

        ResponseEntity<Void> response = alertController.checkTelemetry(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("checkTelemetry deve delegar o processamento para o AlertService")
    void checkTelemetry_deveDelegarParaAlertService() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-02", -5.0, -23.0, -46.0, null);

        alertController.checkTelemetry(dto);

        verify(alertService, times(1)).handleAlert(dto);
    }

    @Test
    @DisplayName("checkTelemetry deve chamar o AlertService exatamente uma vez")
    void checkTelemetry_deveChamarServiceUmaVez() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-03", 10.0, 0.0, 0.0, null);

        alertController.checkTelemetry(dto);

        verify(alertService, times(1)).handleAlert(any(TelemetryDTO.class));
        verifyNoMoreInteractions(alertService);
    }

    @Test
    @DisplayName("checkTelemetry com temperatura critica deve retornar 200 OK")
    void checkTelemetry_comTemperaturaCritica_deveRetornar200() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-04", 50.0, -23.55, -46.63, null);

        ResponseEntity<Void> response = alertController.checkTelemetry(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
