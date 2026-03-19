package com.saferoute.sensor_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.saferoute.sensor_service.client.AlertClient;
import com.saferoute.sensor_service.dto.TelemetryDTO;

@Service
public class TelemetryService {

  @Autowired
  private AlertClient alertClient;

  // Processa os dados e encaminha para analise
  public void processTelemetry(TelemetryDTO data) {
    alertClient.sendToAnalysis(data);
  }
}
