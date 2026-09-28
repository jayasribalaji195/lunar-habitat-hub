package com.lunarhub.dto;

import java.math.BigDecimal;

public class CreateModuleRequest {
    private String name;
    private String zone;
    private BigDecimal domeAreaM2;
    private BigDecimal pressureMinKPa;
    private BigDecimal pressureMaxKPa;
    private BigDecimal waterPurityMinPct;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public BigDecimal getDomeAreaM2() { return domeAreaM2; }
    public void setDomeAreaM2(BigDecimal domeAreaM2) { this.domeAreaM2 = domeAreaM2; }
    public BigDecimal getPressureMinKPa() { return pressureMinKPa; }
    public void setPressureMinKPa(BigDecimal pressureMinKPa) { this.pressureMinKPa = pressureMinKPa; }
    public BigDecimal getPressureMaxKPa() { return pressureMaxKPa; }
    public void setPressureMaxKPa(BigDecimal pressureMaxKPa) { this.pressureMaxKPa = pressureMaxKPa; }
    public BigDecimal getWaterPurityMinPct() { return waterPurityMinPct; }
    public void setWaterPurityMinPct(BigDecimal waterPurityMinPct) { this.waterPurityMinPct = waterPurityMinPct; }
}
