package com.saferoute.sensor_service.service;

import com.saferoute.sensor_service.client.AlertClient;
import com.saferoute.sensor_service.dto.TelemetryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TelemetryService")
class TelemetryServiceTest {

    @Mock
    private AlertClient alertClient;

    @InjectMocks
    private TelemetryService telemetryService;

    @Test
    @DisplayName("processTelemetry deve encaminhar os dados para o AlertClient")
    void processTelemetry_deveEncaminharParaAlertClient() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", 5.0, -23.55, -46.63, "2024-01-01T10:00:00");

        telemetryService.processTelemetry(dto);

        verify(alertClient, times(1)).sendToAnalysis(dto);
    }

    @Test
    @DisplayName("processTelemetry deve chamar AlertClient exatamente uma vez")
    void processTelemetry_deveChamarAlertClientExatamenteUmaVez() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-02", -3.0, -23.0, -46.0, null);

        telemetryService.processTelemetry(dto);

        verify(alertClient, times(1)).sendToAnalysis(any(TelemetryDTO.class));
        verifyNoMoreInteractions(alertClient);
    }

    @Test
    @DisplayName("processTelemetry nao deve lancar excecao com dados validos")
    void processTelemetry_naoDeveLancarExcecao() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-03", 10.0, -23.5, -46.6, "2024-01-01");
        doNothing().when(alertClient).sendToAnalysis(any());

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> telemetryService.processTelemetry(dto)
        );
    }
}
