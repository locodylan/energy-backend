package com.dysight.backend.dto;

import java.time.LocalDateTime;

public class ConsumoEnergiaResponseDTO {

    private Long id;
    private String dispositivo;
    private Long dispositivoIoTId;

    private Boolean encendido;

    private Double voltajeV;
    private Double corrienteA;
    private Double potenciaW;
    private Double energiaWh;
    private Double frecuenciaHz;
    private Double temperaturaC;

    private LocalDateTime fechaHora;

    public ConsumoEnergiaResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public Long getDispositivoIoTId() {
        return dispositivoIoTId;
    }

    public void setDispositivoIoTId(Long dispositivoIoTId) {
        this.dispositivoIoTId = dispositivoIoTId;
    }

    public Boolean getEncendido() {
        return encendido;
    }

    public void setEncendido(Boolean encendido) {
        this.encendido = encendido;
    }

    public Double getVoltajeV() {
        return voltajeV;
    }

    public void setVoltajeV(Double voltajeV) {
        this.voltajeV = voltajeV;
    }

    public Double getCorrienteA() {
        return corrienteA;
    }

    public void setCorrienteA(Double corrienteA) {
        this.corrienteA = corrienteA;
    }

    public Double getPotenciaW() {
        return potenciaW;
    }

    public void setPotenciaW(Double potenciaW) {
        this.potenciaW = potenciaW;
    }

    public Double getEnergiaWh() {
        return energiaWh;
    }

    public void setEnergiaWh(Double energiaWh) {
        this.energiaWh = energiaWh;
    }

    public Double getFrecuenciaHz() {
        return frecuenciaHz;
    }

    public void setFrecuenciaHz(Double frecuenciaHz) {
        this.frecuenciaHz = frecuenciaHz;
    }

    public Double getTemperaturaC() {
        return temperaturaC;
    }

    public void setTemperaturaC(Double temperaturaC) {
        this.temperaturaC = temperaturaC;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}