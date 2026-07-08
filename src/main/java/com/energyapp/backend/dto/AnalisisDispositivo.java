package com.energyapp.backend.dto;

public class AnalisisDispositivo {

    private String dispositivo;

    private double promedio;

    private double actual;

    private double porcentaje;

    private String estado;

    private String mensaje;

    public AnalisisDispositivo(
            String dispositivo,
            double promedio,
            double actual,
            double porcentaje,
            String estado,
            String mensaje
    ) {
        this.dispositivo = dispositivo;
        this.promedio = promedio;
        this.actual = actual;
        this.porcentaje = porcentaje;
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public double getPromedio() {
        return promedio;
    }

    public double getActual() {
        return actual;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public String getEstado() {
        return estado;
    }

    public String getMensaje() {
        return mensaje;
    }
}