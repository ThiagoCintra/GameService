package com.gameservice.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "level_rule")
public class LevelRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer level;

    private Long minPoints;

    public LevelRule() {}

    public LevelRule(Integer level, Long minPoints) {
        this.level = level;
        this.minPoints = minPoints;
    }

    public Long getId() { return id; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Long getMinPoints() { return minPoints; }
    public void setMinPoints(Long minPoints) { this.minPoints = minPoints; }
}
