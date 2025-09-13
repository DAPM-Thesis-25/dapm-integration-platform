package com.dapm.security_service.models;

import com.dapm.security_service.models.enums.Tier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tiers {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private Tier name;

    @Column(name = "max_duration_hours", nullable = false, unique = false)
    private Integer maxHours;

}
