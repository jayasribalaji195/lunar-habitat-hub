package com.lunarhub.model;

import jakarta.persistence.*;

@Entity
@Table(name = "contacts")
public class Contact {
    public enum ContactType { CUSTOMER, VENDOR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. "Aurora Regolith Mining Co." (customer) or "Selene Sensor Systems" (vendor) */
    private String name;

    @Enumerated(EnumType.STRING)
    private ContactType type;

    private String phone;
    private String email;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ContactType getType() { return type; }
    public void setType(ContactType type) { this.type = type; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
