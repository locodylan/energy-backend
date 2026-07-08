package com.energyapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ConsumoEnergia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dispositivo;

    private Double consumo;

    private LocalDateTime fechaHora;
    private boolean recomendacionOculta = false;
private LocalDateTime recomendacionCreatedAt;


    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public ConsumoEnergia() {
    }

    public Long getId() {
        return id;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public Double getConsumo() {
        return consumo;
    }

    public void setConsumo(double consumo) {
        this.consumo = consumo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public boolean isRecomendacionOculta() {
return recomendacionOculta;
}
public LocalDateTime getRecomendacionCreatedAt() {
    return recomendacionCreatedAt;
}

public void setRecomendacionCreatedAt(LocalDateTime recomendacionCreatedAt) {
    this.recomendacionCreatedAt = recomendacionCreatedAt;
}

public void setRecomendacionOculta(
boolean recomendacionOculta
) {
this.recomendacionOculta =
recomendacionOculta;
}


    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

}