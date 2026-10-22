package com.dysight.backend.dto;

public class PrediccionConsumo {

    private double prediccion;

    public PrediccionConsumo() {
    }

    public PrediccionConsumo(double prediccion) {
        this.prediccion = prediccion;
    }

    public double getPrediccion() {
        return prediccion;
    }

    public void setPrediccion(double prediccion) {
        this.prediccion = prediccion;
    }
}