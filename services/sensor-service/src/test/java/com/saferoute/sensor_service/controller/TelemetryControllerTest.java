package com.saferoute.sensor_service.controller;

import com.saferoute.sensor_service.dto.TelemetryDTO;
import com.saferoute.sensor_service.service.TelemetryService;
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
@DisplayName("TelemetryController")
class TelemetryControllerTest {

    @Mock
    private TelemetryService telemetryService;

    @InjectMocks
    private TelemetryController telemetryController;

    // ------------------------------------------------------------------ //
    // receiveData
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("receiveData deve retornar 200 OK com dados validos")
    void receiveData_deveRetornar200() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", 5.0, -23.55, -46.63, "2024-01-01T10:00:00");

        ResponseEntity<String> response = telemetryController.receiveData(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Processado com sucesso.", response.getBody());
    }

    @Test
    @DisplayName("receiveData deve chamar o servico de telemetria")
    void receiveData_deveChamarServico() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-02", 3.0, -23.0, -46.0, null);

        telemetryController.receiveData(dto);

        verify(telemetryService, times(1)).processTelemetry(dto);
    }

    @Test
    @DisplayName("receiveData deve funcionar com temperatura negativa")
    void receiveData_comTemperaturaNegativa() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-03", -1.0, -23.0, -46.0, null);

        ResponseEntity<String> response = telemetryController.receiveData(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(telemetryService).processTelemetry(dto);
    }

    // ------------------------------------------------------------------ //
    // fallbackAlert
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("fallbackAlert deve retornar 202 ACCEPTED")
    void fallbackAlert_deveRetornar202() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", 5.0, -23.55, -46.63, null);
        Throwable causa = new RuntimeException("Alert-Service indisponivel");

        ResponseEntity<String> response = telemetryController.fallbackAlert(dto, causa);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    @DisplayName("fallbackAlert deve retornar mensagem de modo de seguranca")
    void fallbackAlert_deveRetornarMensagemDeModoSeguranca() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-99", 20.0, 0.0, 0.0, null);

        ResponseEntity<String> response = telemetryController.fallbackAlert(dto, new RuntimeException());

        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Modo de Segurança")
                || response.getBody().contains("Modo de Seguranca")
                || response.getBody().contains("espera"));
    }

    @Test
    @DisplayName("fallbackAlert nao deve chamar o servico de telemetria")
    void fallbackAlert_naoDeveChamarServico() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", 5.0, -23.55, -46.63, null);

        telemetryController.fallbackAlert(dto, new RuntimeException());

        verifyNoInteractions(telemetryService);
    }
}
