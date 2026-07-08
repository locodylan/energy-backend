package com.energyapp.backend.dto;

public class DispositivoIoT {

    private String nombre;
    private boolean encendido;

    public DispositivoIoT(
            String nombre,
            boolean encendido) {
        this.nombre = nombre;
        this.encendido = encendido;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isEncendido() {
        return encendido;
    }

    public void setEncendido(
            boolean encendido) {
        this.encendido = encendido;
    }
}