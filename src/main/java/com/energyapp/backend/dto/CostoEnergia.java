package com.energyapp.backend.dto;

public class CostoEnergia {

    private double diario;
    private double mensual;
    private double anual;

    public CostoEnergia(
            double diario,
            double mensual,
            double anual
    ) {

        this.diario = diario;
        this.mensual = mensual;
        this.anual = anual;
    }

    public double getDiario() {
        return diario;
    }

    public double getMensual() {
        return mensual;
    }

    public double getAnual() {
        return anual;
    }
}