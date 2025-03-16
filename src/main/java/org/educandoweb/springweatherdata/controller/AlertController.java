package org.educandoweb.springweatherdata.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.educandoweb.springweatherdata.entities.WeatherAlert.AlertType;
import org.educandoweb.springweatherdata.responses.AlertResponse;
import org.educandoweb.springweatherdata.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather/alerts")
@Tag(name = "Weather Alerts", description = "Endpoints for weather alert management")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(
            @RequestParam String city,
            @RequestParam AlertType alertType,
            @RequestParam Double thresholdValue,
            @AuthenticationPrincipal UserDetails userDetails) {
        AlertResponse alert = alertService.createAlert(city, alertType, thresholdValue, userDetails.getUsername());
        return ResponseEntity.ok(alert);
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getUserAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AlertResponse> alerts = alertService.getUserAlerts(userDetails.getUsername());
        return ResponseEntity.ok(alerts);
    }

    @PutMapping("/{alertId}")
    public ResponseEntity<AlertResponse> toggleAlert(
            @PathVariable Long alertId,
            @RequestParam boolean active,
            @AuthenticationPrincipal UserDetails userDetails) {
        AlertResponse alert = alertService.toggleAlert(alertId, active, userDetails.getUsername());
        return ResponseEntity.ok(alert);
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deleteAlert(
            @PathVariable Long alertId,
            @AuthenticationPrincipal UserDetails userDetails) {
        alertService.deleteAlert(alertId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check")
    public ResponseEntity<List<AlertResponse>> checkAlertsForCity(
            @RequestParam String city,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AlertResponse> triggeredAlerts = alertService.checkAlertsForCity(city, userDetails.getUsername());
        return ResponseEntity.ok(triggeredAlerts);
    }
}