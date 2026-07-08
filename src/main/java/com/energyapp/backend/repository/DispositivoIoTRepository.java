package com.energyapp.backend.repository;

import com.energyapp.backend.model.DispositivoIoT;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispositivoIoTRepository
extends JpaRepository<DispositivoIoT, Long> {

List<DispositivoIoT>
    findByUsuarioId(Long usuarioId);
DispositivoIoT findByNombreAndUsuarioId( String nombre, Long usuarioId );
DispositivoIoT findByNombre(String nombre);
}
