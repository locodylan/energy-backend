package com.dysight.backend.dto;

import com.dysight.backend.model.DiaSemana;
import com.dysight.backend.model.EstadoProgramacion;

import java.time.LocalTime;
import java.util.Set;

public class ProgramacionHorarioDTO {

    private Long id;
    private Long dispositivoId;
    private String dispositivoNombre;
    private String aparatoConectado;
    private String categoria;
    private Set<DiaSemana> dias;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoProgramacion estado;

    public ProgramacionHorarioDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDispositivoId() {
        return dispositivoId;
    }

    public void setDispositivoId(Long dispositivoId) {
        this.dispositivoId = dispositivoId;
    }

    public String getDispositivoNombre() {
        return dispositivoNombre;
    }

    public void setDispositivoNombre(String dispositivoNombre) {
        this.dispositivoNombre = dispositivoNombre;
    }

    public String getAparatoConectado() {
        return aparatoConectado;
    }

    public void setAparatoConectado(String aparatoConectado) {
        this.aparatoConectado = aparatoConectado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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