package com.saferoute.alert_service.service;

import com.saferoute.alert_service.dto.TelemetryDTO;
import com.saferoute.alert_service.model.Alert;
import com.saferoute.alert_service.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService")
class AlertServiceTest {

    // Limites definidos em alert-service.yml (temp-min: -2.0, temp-max: 8.0)
    private static final double TEMP_MIN = -2.0;
    private static final double TEMP_MAX =  8.0;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    @BeforeEach
    void setUp() {
        // Injeta os valores de @Value sem subir o contexto Spring
        ReflectionTestUtils.setField(alertService, "tempMin", TEMP_MIN);
        ReflectionTestUtils.setField(alertService, "tempMax", TEMP_MAX);
    }

    // ------------------------------------------------------------------ //
    // isTemperatureAlert
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("isTemperatureAlert deve retornar false para dados nulos")
    void isTemperatureAlert_dadosNulos_deveRetornarFalse() {
        assertFalse(alertService.isTemperatureAlert(null));
    }

    @Test
    @DisplayName("isTemperatureAlert deve retornar false quando temperatura e nula")
    void isTemperatureAlert_temperaturaNula_deveRetornarFalse() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", null, -23.55, -46.63, null);
        assertFalse(alertService.isTemperatureAlert(dto));
    }

    @Test
    @DisplayName("isTemperatureAlert deve retornar true abaixo do minimo")
    void isTemperatureAlert_abaixoDoMinimo_deveRetornarTrue() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", -3.0, -23.55, -46.63, null);
        assertTrue(alertService.isTemperatureAlert(dto));
    }

    @Test
    @DisplayName("isTemperatureAlert deve retornar true acima do maximo")
    void isTemperatureAlert_acimaDoMaximo_deveRetornarTrue() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", 9.0, -23.55, -46.63, null);
        assertTrue(alertService.isTemperatureAlert(dto));
    }

    @Test
    @DisplayName("isTemperatureAlert deve retornar false dentro dos limites")
    void isTemperatureAlert_dentroDosLimites_deveRetornarFalse() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", 5.0, -23.55, -46.63, null);
        assertFalse(alertService.isTemperatureAlert(dto));
    }

    @Test
    @DisplayName("isTemperatureAlert deve retornar false exatamente no minimo")
    void isTemperatureAlert_exatamenteNoMinimo_deveRetornarFalse() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", TEMP_MIN, -23.55, -46.63, null);
        assertFalse(alertService.isTemperatureAlert(dto));
    }

    @Test
    @DisplayName("isTemperatureAlert deve retornar false exatamente no maximo")
    void isTemperatureAlert_exatamenteNoMaximo_deveRetornarFalse() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", TEMP_MAX, -23.55, -46.63, null);
        assertFalse(alertService.isTemperatureAlert(dto));
    }

    // ------------------------------------------------------------------ //
    // handleAlert
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("handleAlert nao deve salvar quando dados sao nulos")
    void handleAlert_dadosNulos_naoDeveSalvar() {
        alertService.handleAlert(null);
        verifyNoInteractions(alertRepository);
    }

    @Test
    @DisplayName("handleAlert nao deve salvar quando temperatura e nula")
    void handleAlert_temperaturaNula_naoDeveSalvar() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", null, -23.55, -46.63, null);

        alertService.handleAlert(dto);

        verifyNoInteractions(alertRepository);
    }

    @Test
    @DisplayName("handleAlert deve salvar alerta quando temperatura abaixo do minimo")
    void handleAlert_abaixoDoMinimo_deveSalvarAlerta() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", -5.0, -23.55, -46.63, null);

        alertService.handleAlert(dto);

        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("handleAlert deve salvar alerta quando temperatura acima do maximo")
    void handleAlert_acimaDoMaximo_deveSalvarAlerta() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", 15.0, -23.55, -46.63, null);

        alertService.handleAlert(dto);

        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("handleAlert nao deve salvar quando temperatura dentro dos limites")
    void handleAlert_dentroDosLimites_naoDeveSalvar() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", 5.0, -23.55, -46.63, null);

        alertService.handleAlert(dto);

        verifyNoInteractions(alertRepository);
    }

    @Test
    @DisplayName("handleAlert deve salvar Alert com os dados corretos do DTO")
    void handleAlert_deveSalvarAlertComDadosCorretos() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-42", -10.0, -23.5, -46.6, null);
        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);

        alertService.handleAlert(dto);

        verify(alertRepository).save(captor.capture());
        Alert salvo = captor.getValue();
        assertEquals("TRUCK-42", salvo.getTruckId());
        assertEquals(-10.0,      salvo.getTemperature());
        assertEquals(-23.5,      salvo.getLatitude());
        assertEquals(-46.6,      salvo.getLongitude());
        assertNotNull(salvo.getTimestamp());
    }
}
