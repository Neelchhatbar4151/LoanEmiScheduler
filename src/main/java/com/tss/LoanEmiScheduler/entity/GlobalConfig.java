package com.tss.LoanEmiScheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

@Table(name = "global_configs")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GlobalConfig {
    @Id
    @Column(nullable = false, unique = true)
    private String key;
    @Column(nullable = false)
    private String value;
}
