package com.dysight.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verificaciones_correo")
public class VerificacionCorreo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String codigo;

    private LocalDateTime expiracion;

    private boolean activo;
    private boolean verificado;

    public VerificacionCorreo() {
    }

    public VerificacionCorreo(
            String email,
            String codigo,
            LocalDateTime expiracion
    ) {
        this.email = email;
        this.codigo = codigo;
        this.expiracion = expiracion;
        this.activo = true;
        this.verificado = false;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getExpiracion() {
        return expiracion;
    }

    public void setExpiracion(LocalDateTime expiracion) {
        this.expiracion = expiracion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isVerificado() {
    return verificado;
}

public void setVerificado(boolean verificado) {
    this.verificado = verificado;
}

}