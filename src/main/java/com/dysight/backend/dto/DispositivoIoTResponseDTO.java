package com.dysight.backend.dto;

public class DispositivoIoTResponseDTO {

    private Long id;
    private String nombre;
    private String aparatoConectado;
    private String tipo;
    private String categoria;
    private boolean encendido;
    private String ipShelly;
    private String shellyId;
    private String mac;
    private Long usuarioId;

    public DispositivoIoTResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAparatoConectado() {
    return aparatoConectado;
}

public void setAparatoConectado(String aparatoConectado) {
    this.aparatoConectado = aparatoConectado;
}

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public boolean isEncendido() {
        return encendido;
    }

    public void setEncendido(boolean encendido) {
        this.encendido = encendido;
    }

    public String getIpShelly() {
        return ipShelly;
    }

    public void setIpShelly(String ipShelly) {
        this.ipShelly = ipShelly;
    }

    public String getShellyId() {
        return shellyId;
    }

    public void setShellyId(String shellyId) {
        this.shellyId = shellyId;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}