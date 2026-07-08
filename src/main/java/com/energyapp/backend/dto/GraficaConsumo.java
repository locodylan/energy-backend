package com.energyapp.backend.dto;

public class GraficaConsumo {

    private String fecha;
    private double consumo;

    public GraficaConsumo() {
    }

    public GraficaConsumo(String fecha, double consumo) {
        this.fecha = fecha;
        this.consumo = consumo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getConsumo() {
        return consumo;
    }

    public void setConsumo(double consumo) {
        this.consumo = consumo;
    }
}