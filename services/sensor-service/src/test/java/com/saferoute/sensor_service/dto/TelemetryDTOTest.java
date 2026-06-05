package com.saferoute.sensor_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TelemetryDTO - sensor-service")
class TelemetryDTOTest {

    @Test
    @DisplayName("Construtor vazio deve criar objeto com campos nulos")
    void constructorVazio_deveCriarObjetoComCamposNulos() {
        TelemetryDTO dto = new TelemetryDTO();
        assertNull(dto.getTruckId());
        assertNull(dto.getTemperature());
        assertNull(dto.getLatitude());
        assertNull(dto.getLongitude());
        assertNull(dto.getTimestamp());
    }

    @Test
    @DisplayName("Construtor completo deve preencher todos os campos")
    void constructorCompleto_devePreencherTodosOsCampos() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-01", 5.0, -23.55, -46.63, "2024-01-01T10:00:00");

        assertEquals("TRUCK-01", dto.getTruckId());
        assertEquals(5.0,        dto.getTemperature());
        assertEquals(-23.55,     dto.getLatitude());
        assertEquals(-46.63,     dto.getLongitude());
        assertEquals("2024-01-01T10:00:00", dto.getTimestamp());
    }

    @Test
    @DisplayName("Setters devem atualizar os campos corretamente")
    void setters_devemAtualizarCampos() {
        TelemetryDTO dto = new TelemetryDTO();

        dto.setTruckId("TRUCK-99");
        dto.setTemperature(3.5);
        dto.setLatitude(-23.0);
        dto.setLongitude(-46.0);
        dto.setTimestamp("2024-06-01T08:00:00");

        assertEquals("TRUCK-99", dto.getTruckId());
        assertEquals(3.5,        dto.getTemperature());
        assertEquals(-23.0,      dto.getLatitude());
        assertEquals(-46.0,      dto.getLongitude());
        assertEquals("2024-06-01T08:00:00", dto.getTimestamp());
    }

    @Test
    @DisplayName("Temperatura negativa deve ser armazenada corretamente")
    void temperaturaNegativa_deveSerArmazenadaCorretamente() {
        TelemetryDTO dto = new TelemetryDTO("TRUCK-02", -5.0, -23.55, -46.63, null);
        assertEquals(-5.0, dto.getTemperature());
    }
}
