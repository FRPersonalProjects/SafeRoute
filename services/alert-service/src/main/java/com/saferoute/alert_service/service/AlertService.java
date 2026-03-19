package com.saferoute.alert_service.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.saferoute.alert_service.dto.TelemetryDTO;
import com.saferoute.alert_service.model.Alert;
import com.saferoute.alert_service.repository.AlertRepository;

@Service

public class AlertService {

  @Value("${saferoute.temp-min}")
  private Double tempMin;

  @Value("${saferoute.temp-max}")
  private Double tempMax;

  @Autowired
  private AlertRepository alertRepository;

  // Verifica se a temperatura está fora dos limites
  public boolean isTemperatureAlert(TelemetryDTO data) {
    if (data == null || data.getTemperature() == null) {
      return false;
    }
    double temp = data.getTemperature();
    return temp < tempMin || temp > tempMax;
  }

  // Lida com o alerta (salva no banco se necessário)
  public void handleAlert(TelemetryDTO data) {
    if (data == null || data.getTemperature() == null) {
      System.out.println("Dados de telemetria inválidos.");
      return;
    }
    double temp = data.getTemperature();
    
    if (temp < tempMin) {
      System.out.println(
          "Caminhão: " + data.getTruckId() + " | Temperatura: " + temp + "°C (abaixo do mínimo: " + tempMin + "°C)");
      saveAlert(data);

    } else if (temp > tempMax) {
      System.out.println(
          "Caminhão: " + data.getTruckId() + " | Temperatura: " + temp + "°C (acima do máximo: " + tempMax + "°C)");
      saveAlert(data);

    } else {
      System.out.println(
        "Caminhão: " + data.getTruckId() + " | Temperatura: " + temp + "°C (dentro dos limites)");
    }
  }

  // Salva alerta no banco
  private void saveAlert(TelemetryDTO data) {
    Alert alert = new Alert();
    alert.setTruckId(data.getTruckId());
    alert.setTemperature(data.getTemperature());
    alert.setLatitude(data.getLatitude());
    alert.setLongitude(data.getLongitude());
    alert.setTimestamp(LocalDateTime.now());
    alertRepository.save(alert);
  }
}
