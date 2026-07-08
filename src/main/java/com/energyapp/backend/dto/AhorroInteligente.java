package com.energyapp.backend.dto;

public class AhorroInteligente {

    private double consumoTotal;
    private double consumoAhorrado;
    private double dineroAhorrado;
    private double eficiencia;
    private String mensajeIA;

    public AhorroInteligente(
            double consumoTotal,
            double consumoAhorrado,
            double dineroAhorrado,
            double eficiencia,
            String mensajeIA
    ) {

        this.consumoTotal = consumoTotal;
        this.consumoAhorrado = consumoAhorrado;
        this.dineroAhorrado = dineroAhorrado;
        this.eficiencia = eficiencia;
        this.mensajeIA = mensajeIA;
    }

    public double getConsumoTotal() {
        return consumoTotal;
    }

    public double getConsumoAhorrado() {
        return consumoAhorrado;
    }

    public double getDineroAhorrado() {
        return dineroAhorrado;
    }

    public double getEficiencia() {
        return eficiencia;
    }

    public String getMensajeIA() {
        return mensajeIA;
    }
}