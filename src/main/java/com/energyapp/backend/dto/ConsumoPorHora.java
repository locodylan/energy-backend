package com.energyapp.backend.dto;

public class ConsumoPorHora {

    private String hora;
    private double totalConsumo;

    public ConsumoPorHora() {
    }

    public ConsumoPorHora(String hora, double totalConsumo) {
        this.hora = hora;
        this.totalConsumo = totalConsumo;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public double getTotalConsumo() {
        return totalConsumo;
    }

    public void setTotalConsumo(double totalConsumo) {
        this.totalConsumo = totalConsumo;
    }
}