package com.lunarhub.dto;

import java.math.BigDecimal;

public class TelemetryRequest {
    private Long moduleId;
    private BigDecimal atmosphericPressureKPa;
    private BigDecimal waterPurityPct;
    private BigDecimal oxygenLevelPct;
    private BigDecimal co2LevelPpm;
    private BigDecimal oxygenConsumedM3;
    private BigDecimal waterConsumedLiters;

    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }
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
}
