package com.energyapp.backend.model;

import jakarta.persistence.*;

@Entity
public class DispositivoIoT {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String nombre;

private boolean encendido;

@ManyToOne
@JoinColumn(name = "usuario_id")
private Usuario usuario;

public DispositivoIoT() {
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

public boolean isEncendido() {
    return encendido;
}

public void setEncendido(boolean encendido) {
    this.encendido = encendido;
}

public Usuario getUsuario() {
    return usuario;
}

public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
}

}
