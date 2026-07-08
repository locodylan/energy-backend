package com.energyapp.backend.dto;

public class ConsumoPorDispositivo {

    private String dispositivo;
    private double totalConsumo;
    private boolean encendido;
    public ConsumoPorDispositivo() {
    }

    public ConsumoPorDispositivo(String dispositivo, double totalConsumo, boolean encendido) {
        this.dispositivo = dispositivo;
        this.totalConsumo = totalConsumo;
        this.encendido = encendido;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public double getTotalConsumo() {
        return totalConsumo;
    }

    public void setTotalConsumo(double totalConsumo) {
        this.totalConsumo = totalConsumo;
    }
    public void setEncendido(boolean encendido){
        this.encendido = encendido;
    }
    public boolean getEncendido(){
        return encendido;
    }
}