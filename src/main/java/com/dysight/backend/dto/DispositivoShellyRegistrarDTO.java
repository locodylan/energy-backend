
package com.dysight.backend.dto;

public class DispositivoShellyRegistrarDTO {

    private String shellyId;
    private String ipShelly;
    private String mac;
    private String tipo;
    private Double energiaInicialWh;
    private String aparatoConectado;
    private String categoria;
    private Long usuarioId;

    public String getShellyId() {
        return shellyId;
    }

    public void setShellyId(String shellyId) {
        this.shellyId = shellyId;
    }

    public String getIpShelly() {
        return ipShelly;
    }

    public void setIpShelly(String ipShelly) {
        this.ipShelly = ipShelly;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getEnergiaInicialWh() {
        return energiaInicialWh;
    }

    public void setEnergiaInicialWh(Double energiaInicialWh) {
        this.energiaInicialWh = energiaInicialWh;
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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}
