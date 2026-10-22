package com.dysight.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ConsumoEnergia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dispositivo;

    @ManyToOne
@JoinColumn(name = "dispositivo_iot_id")
private DispositivoIoT dispositivoIoT;

    // Se mantiene temporalmente para compatibilidad
    // con los endpoints actuales.
    private Double consumo;

    // Mediciones reales provenientes del Shelly
    private Double voltajeV;
    private Double corrienteA;
    private Double potenciaW;
    private Double energiaWh;
    private Double frecuenciaHz;
    private Double temperaturaC;

    private Boolean encendido;

    private LocalDateTime fechaHora;

    private boolean recomendacionOculta = false;

    private LocalDateTime recomendacionCreatedAt;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public ConsumoEnergia() {
    }

    public Long getId() {
        return id;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public Double getConsumo() {
        return consumo;
    }

    public void setConsumo(Double consumo) {
        this.consumo = consumo;
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

    public Boolean isEncendido() {
        return encendido;
    }

    public void setEncendido(Boolean encendido) {
        this.encendido = encendido;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public boolean isRecomendacionOculta() {
        return recomendacionOculta;
    }

    public void setRecomendacionOculta(boolean recomendacionOculta) {
        this.recomendacionOculta = recomendacionOculta;
    }

    public LocalDateTime getRecomendacionCreatedAt() {
        return recomendacionCreatedAt;
    }

    public void setRecomendacionCreatedAt(
            LocalDateTime recomendacionCreatedAt) {

        this.recomendacionCreatedAt =
                recomendacionCreatedAt;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public DispositivoIoT getDispositivoIoT() {
    return dispositivoIoT;
}

public void setDispositivoIoT(DispositivoIoT dispositivoIoT) {
    this.dispositivoIoT = dispositivoIoT;
}

}