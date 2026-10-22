package com.dysight.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_pendientes")
public class RegistroPendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String username;

    private String email;

    private String password;

    private LocalDateTime expiracion;

    public RegistroPendiente() {
    }

    public RegistroPendiente(
            String nombre,
            String username,
            String email,
            String password,
            LocalDateTime expiracion
    ) {
        this.nombre = nombre;
        this.username = username;
        this.email = email;
        this.password = password;
        this.expiracion = expiracion;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getExpiracion() {
        return expiracion;
    }

    public void setExpiracion(LocalDateTime expiracion) {
        this.expiracion = expiracion;
    }
}