package com.energyapp.backend.dto;

public class Recomendacion {

    private String dispositivo;
    private String mensaje;

    public Recomendacion() {
    }

    public Recomendacion(String dispositivo, String mensaje) {
        this.dispositivo = dispositivo;
        this.mensaje = mensaje;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}