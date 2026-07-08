package com.energyapp.backend.dto;

public class AnomaliaConsumo {

    private String dispositivo;
    private double consumo;
    private String mensaje;

    public AnomaliaConsumo() {
    }

    public AnomaliaConsumo(String dispositivo, double consumo, String mensaje) {
        this.dispositivo = dispositivo;
        this.consumo = consumo;
        this.mensaje = mensaje;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public double getConsumo() {
        return consumo;
    }

    public void setConsumo(double consumo) {
        this.consumo = consumo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}