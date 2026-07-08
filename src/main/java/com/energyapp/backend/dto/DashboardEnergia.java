package com.energyapp.backend.dto;

public class DashboardEnergia {

    private double consumoTotal;
    private double consumoPromedio;
    private double prediccionIA;
    private double mayorConsumo;
    private int totalAlertas;

    public DashboardEnergia() {
    }

    public DashboardEnergia(double consumoTotal,
                            double consumoPromedio,
                            double prediccionIA,
                            double mayorConsumo,
                            int totalAlertas) {

        this.consumoTotal = consumoTotal;
        this.consumoPromedio = consumoPromedio;
        this.prediccionIA = prediccionIA;
        this.mayorConsumo = mayorConsumo;
        this.totalAlertas = totalAlertas;
    }

    public double getConsumoTotal() {
        return consumoTotal;
    }

    public void setConsumoTotal(double consumoTotal) {
        this.consumoTotal = consumoTotal;
    }

    public double getConsumoPromedio() {
        return consumoPromedio;
    }

    public void setConsumoPromedio(double consumoPromedio) {
        this.consumoPromedio = consumoPromedio;
    }

    public double getPrediccionIA() {
        return prediccionIA;
    }

    public void setPrediccionIA(double prediccionIA) {
        this.prediccionIA = prediccionIA;
    }

    public double getMayorConsumo() {
        return mayorConsumo;
    }

    public void setMayorConsumo(double mayorConsumo) {
        this.mayorConsumo = mayorConsumo;
    }

    public int getTotalAlertas() {
        return totalAlertas;
    }

    public void setTotalAlertas(int totalAlertas) {
        this.totalAlertas = totalAlertas;
    }
}