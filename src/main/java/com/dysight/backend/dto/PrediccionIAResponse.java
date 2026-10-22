package com.dysight.backend.dto;

public class PrediccionIAResponse {

    private Double prediccion;
    private Double promedio;
    private Double incremento;
    private String estado;

    public PrediccionIAResponse() {
    }

    public Double getPrediccion() {
        return prediccion;
    }

    public void setPrediccion(Double prediccion) {
        this.prediccion = prediccion;
    }

    public Double getPromedio() {
        return promedio;
    }

    public void setPromedio(Double promedio) {
        this.promedio = promedio;
    }

    public Double getIncremento() {
        return incremento;
    }

    public void setIncremento(Double incremento) {
        this.incremento = incremento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}