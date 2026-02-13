package com.elacs.testStudents.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "fit_data")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FitData {
    @Id
    private Long id = 1L;

    @Column(name = "token", length = 500)
    private String token;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "iat")
    private String iat;

    @Column(name = "jti")
    private String jti;

    @Column(name = "gid")
    private String gid;

    @Column(name = "cid")
    private String cid;

    @Column(name = "ecs")
    private String ecs;

    @Column(name = "isr")
    private boolean isr;

    @Column(name = "exp")
    private String exp;
}
