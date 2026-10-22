package com.dysight.backend.dto;

public class DispositivoIoTActualizarDTO {

    private String aparatoConectado;
    private String categoria;

    public DispositivoIoTActualizarDTO() {
    }

    public String getAparatoConectado() {
        return aparatoConectado;
    }

    public void setAparatoConectado(String aparatoConectado) {
        this.aparatoConectado = aparatoConectado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}