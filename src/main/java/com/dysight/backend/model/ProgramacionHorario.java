package com.dysight.backend.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "programacion_horario")
public class ProgramacionHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private DispositivoIoT dispositivo;

    @ElementCollection(targetClass = DiaSemana.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "programacion_horario_dias",
            joinColumns = @JoinColumn(name = "programacion_horario_id")
    )
    @Column(name = "dia", nullable = false)
    private Set<DiaSemana> dias = new HashSet<>();

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProgramacion estado;

    public ProgramacionHorario() {
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

    public Set<DiaSemana> getDias() {
        return dias;
    }

    public void setDias(Set<DiaSemana> dias) {
        this.dias = dias;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public EstadoProgramacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoProgramacion estado) {
        this.estado = estado;
    }
}