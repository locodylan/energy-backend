package com.dysight.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "programacion_uso")
public class ProgramacionUso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private DispositivoIoT dispositivo;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Column(nullable = false)
    private Integer duracionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProgramacion estado;

    public ProgramacionUso() {
    }

    public Long getId() {
        return id;
    }

    public DispositivoIoT getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(DispositivoIoT dispositivo) {
        this.dispositivo = dispositivo;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public EstadoProgramacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoProgramacion estado) {
        this.estado = estado;
    }
}