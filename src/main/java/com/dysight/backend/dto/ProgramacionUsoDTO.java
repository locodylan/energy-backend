package com.dysight.backend.dto;

import com.dysight.backend.model.EstadoProgramacion;

import java.time.LocalDateTime;

public class ProgramacionUsoDTO {

    private Long id;
    private Long dispositivoId;
    private String dispositivoNombre;
    private String aparatoConectado;
    private String categoria;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private Integer duracionMinutos;
    private EstadoProgramacion estado;

    public ProgramacionUsoDTO() {
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