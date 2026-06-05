package com.saferoute.alert_service.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Alert - entidade JPA")
class AlertTest {

    private Alert criarAlert() {
        return new Alert(1L, "TRUCK-01", 5.0, -23.55, -46.63, LocalDateTime.of(2024, 1, 1, 10, 0));
    }

    @Test
    @DisplayName("Construtor vazio deve criar objeto com campos nulos")
    void constructorVazio_deveCriarObjetoVazio() {
        Alert alert = new Alert();
        assertNull(alert.getId());
        assertNull(alert.getTruckId());
        assertNull(alert.getTemperature());
    }

    @Test
    @DisplayName("Construtor completo deve preencher todos os campos")
    void constructorCompleto_devePreencherCampos() {
        LocalDateTime ts = LocalDateTime.of(2024, 1, 1, 10, 0);
        Alert alert = new Alert(1L, "TRUCK-01", 5.0, -23.55, -46.63, ts);

        assertEquals(1L,         alert.getId());
        assertEquals("TRUCK-01", alert.getTruckId());
        assertEquals(5.0,        alert.getTemperature());
        assertEquals(-23.55,     alert.getLatitude());
        assertEquals(-46.63,     alert.getLongitude());
        assertEquals(ts,         alert.getTimestamp());
    }

    @Test
    @DisplayName("Setters devem atualizar os campos corretamente")
    void setters_devemAtualizarCampos() {
        Alert alert = new Alert();
        LocalDateTime ts = LocalDateTime.now();

        alert.setId(2L);
        alert.setTruckId("TRUCK-02");
        alert.setTemperature(-3.0);
        alert.setLatitude(-23.0);
        alert.setLongitude(-46.0);
        alert.setTimestamp(ts);

        assertEquals(2L,         alert.getId());
        assertEquals("TRUCK-02", alert.getTruckId());
        assertEquals(-3.0,       alert.getTemperature());
        assertEquals(-23.0,      alert.getLatitude());
        assertEquals(-46.0,      alert.getLongitude());
        assertEquals(ts,         alert.getTimestamp());
    }

    @Test
    @DisplayName("equals deve retornar true para o mesmo objeto")
    void equals_mesmoObjeto_deveRetornarTrue() {
        Alert alert = criarAlert();
        assertEquals(alert, alert);
    }

    @Test
    @DisplayName("equals deve retornar true para objetos identicos")
    void equals_objetosIdenticos_deveRetornarTrue() {
        LocalDateTime ts = LocalDateTime.of(2024, 1, 1, 10, 0);
        Alert a1 = new Alert(1L, "TRUCK-01", 5.0, -23.55, -46.63, ts);
        Alert a2 = new Alert(1L, "TRUCK-01", 5.0, -23.55, -46.63, ts);
        assertEquals(a1, a2);
    }

    @Test
    @DisplayName("equals deve retornar false para objetos com id diferente")
    void equals_idDiferente_deveRetornarFalse() {
        LocalDateTime ts = LocalDateTime.of(2024, 1, 1, 10, 0);
        Alert a1 = new Alert(1L, "TRUCK-01", 5.0, -23.55, -46.63, ts);
        Alert a2 = new Alert(2L, "TRUCK-01", 5.0, -23.55, -46.63, ts);
        assertNotEquals(a1, a2);
    }

    @Test
    @DisplayName("equals deve retornar false para null")
    void equals_null_deveRetornarFalse() {
        Alert alert = criarAlert();
        assertNotEquals(null, alert);
    }

    @Test
    @DisplayName("equals deve retornar false para objeto de outra classe")
    void equals_outraClasse_deveRetornarFalse() {
        Alert alert = criarAlert();
        assertNotEquals("string", alert);
    }

    @Test
    @DisplayName("hashCode deve ser igual para objetos iguais")
    void hashCode_objetosIguais_deveTerHashCodeIgual() {
        LocalDateTime ts = LocalDateTime.of(2024, 1, 1, 10, 0);
        Alert a1 = new Alert(1L, "TRUCK-01", 5.0, -23.55, -46.63, ts);
        Alert a2 = new Alert(1L, "TRUCK-01", 5.0, -23.55, -46.63, ts);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    @DisplayName("getSerialversionuid nao deve lancar excecao")
    void getSerialversionuid_naoDeveLancarExcecao() {
        assertDoesNotThrow(Alert::getSerialversionuid);
    }
}
