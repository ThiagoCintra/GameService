package com.gameservice.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "mission")
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String product;

    private java.math.BigDecimal minValue;

    private java.math.BigDecimal maxValue;

    private Long points;

    private boolean active = true;

    public Mission() {}

    public Long getId() {
        return id;
    }

    // Setter for tests and frameworks that require setting id
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public java.math.BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(java.math.BigDecimal minValue) {
        this.minValue = minValue;
    }

    public java.math.BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(java.math.BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
