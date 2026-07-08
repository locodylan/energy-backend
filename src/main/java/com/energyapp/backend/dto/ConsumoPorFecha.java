package com.energyapp.backend.dto;

public class ConsumoPorFecha {

    private String fecha;
    private double total;

    public ConsumoPorFecha() {
    }

    public ConsumoPorFecha(String fecha, double total) {
        this.fecha = fecha;
        this.total = total;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}