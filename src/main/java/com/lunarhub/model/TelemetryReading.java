package com.lunarhub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry_readings")
public class TelemetryReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private HabitatModule module;

    private LocalDateTime timestamp;

    private BigDecimal atmosphericPressureKPa;
    private BigDecimal waterPurityPct;
    private BigDecimal oxygenLevelPct;
    private BigDecimal co2LevelPpm;

    /** Consumption since the previous reading, used to compute utility bills. */
    private BigDecimal oxygenConsumedM3 = BigDecimal.ZERO;
    private BigDecimal waterConsumedLiters = BigDecimal.ZERO;

    /** True if this reading fell outside nominal thresholds and the system auto-adjusted scrubbers. */
    private boolean scrubberAdjusted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public HabitatModule getModule() { return module; }
    public void setModule(HabitatModule module) { this.module = module; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public BigDecimal getAtmosphericPressureKPa() { return atmosphericPressureKPa; }
    public void setAtmosphericPressureKPa(BigDecimal atmosphericPressureKPa) { this.atmosphericPressureKPa = atmosphericPressureKPa; }
    public BigDecimal getWaterPurityPct() { return waterPurityPct; }
    public void setWaterPurityPct(BigDecimal waterPurityPct) { this.waterPurityPct = waterPurityPct; }
    public BigDecimal getOxygenLevelPct() { return oxygenLevelPct; }
    public void setOxygenLevelPct(BigDecimal oxygenLevelPct) { this.oxygenLevelPct = oxygenLevelPct; }
    public BigDecimal getCo2LevelPpm() { return co2LevelPpm; }
    public void setCo2LevelPpm(BigDecimal co2LevelPpm) { this.co2LevelPpm = co2LevelPpm; }
    public BigDecimal getOxygenConsumedM3() { return oxygenConsumedM3; }
    public void setOxygenConsumedM3(BigDecimal oxygenConsumedM3) { this.oxygenConsumedM3 = oxygenConsumedM3; }
    public BigDecimal getWaterConsumedLiters() { return waterConsumedLiters; }
    public void setWaterConsumedLiters(BigDecimal waterConsumedLiters) { this.waterConsumedLiters = waterConsumedLiters; }
    public boolean isScrubberAdjusted() { return scrubberAdjusted; }
    public void setScrubberAdjusted(boolean scrubberAdjusted) { this.scrubberAdjusted = scrubberAdjusted; }
}
