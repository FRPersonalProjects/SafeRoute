package com.saferoute.sensor_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.saferoute.sensor_service.dto.TelemetryDTO;

@FeignClient(name = "ALERT-SERVICE") // Nome no Eureka
public interface AlertClient {

  // Manda os dados para o Alert-Service analisar
  @PostMapping("/alerts/check")
  void sendToAnalysis(@RequestBody TelemetryDTO data);
}
