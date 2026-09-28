package com.lunarhub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "habitat_modules")
public class HabitatModule {
    public enum ModuleStatus { NOMINAL, ALERT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. "Habitat Dome Alpha" */
    private String name;

    /** Lunar facility zone, e.g. "Shackleton Rim Zone 2" */
    private String zone;

    private BigDecimal domeAreaM2;

    /** Nominal atmospheric pressure band, in kPa */
    private BigDecimal pressureMinKPa;
    private BigDecimal pressureMaxKPa;

    /** Minimum acceptable reclaimed-water purity, in percent */
    private BigDecimal waterPurityMinPct;

    @Enumerated(EnumType.STRING)
    private ModuleStatus status = ModuleStatus.NOMINAL;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public ModuleStatus getStatus() { return status; }
    public void setStatus(ModuleStatus status) { this.status = status; }
}
