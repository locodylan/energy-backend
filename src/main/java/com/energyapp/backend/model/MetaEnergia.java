package com.energyapp.backend.model;

import jakarta.persistence.*;

@Entity
public class MetaEnergia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double limiteMensual;

    private boolean modoAhorro;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public MetaEnergia() {
    }

    public Long getId() {
        return id;
    }

    public double getLimiteMensual() {
        return limiteMensual;
    }

    public void setLimiteMensual(double limiteMensual) {
        this.limiteMensual = limiteMensual;
    }

    public boolean isModoAhorro() {
        return modoAhorro;
    }

    public void setModoAhorro(boolean modoAhorro) {
        this.modoAhorro = modoAhorro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}