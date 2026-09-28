package com.lunarhub.model;

import jakarta.persistence.*;

@Entity
@Table(name = "analytic_accounts")
public class AnalyticAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Lunar facility zone, e.g. "Habitat Dome Alpha" */
    private String name;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private HabitatModule module;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public HabitatModule getModule() { return module; }
    public void setModule(HabitatModule module) { this.module = module; }
}
