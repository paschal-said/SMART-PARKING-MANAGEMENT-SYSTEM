package com.anpr.parking.parking;

import jakarta.persistence.*;

@Entity
@Table(name = "floors")
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "floor_id")
    private Long id;

    @Column(name = "floor_name", nullable = false, unique = true)
    private String name;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
}
