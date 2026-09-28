package com.lunarhub.controller;

import com.lunarhub.dto.TelemetryRequest;
import com.lunarhub.model.TelemetryReading;
import com.lunarhub.service.TelemetryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lunar")
@CrossOrigin(origins = "*")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping("/telemetry")
    public TelemetryReading ingest(@RequestBody TelemetryRequest request) {
        return telemetryService.ingest(request);
    }

    @GetMapping("/modules/{moduleId}/telemetry")
    public List<TelemetryReading> history(@PathVariable Long moduleId) {
        return telemetryService.history(moduleId);
    }
}
