package com.dysight.backend.service;

import com.dysight.backend.model.RegistroPendiente;
import com.dysight.backend.model.Usuario;
import com.dysight.backend.repository.RegistroPendienteRepository;
import com.dysight.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RegistroService {

    private final RegistroPendienteRepository registroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;

    public RegistroService(
            RegistroPendienteRepository registroRepository,
            UsuarioRepository usuarioRepository,
            PasswordService passwordService
    ) {
        this.registroRepository = registroRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
    }

    public boolean guardarRegistroPendiente(
            String nombre,
            String username,
            String email,
            String password
    ) {

        if (usuarioRepository.findByUsername(username) != null) {
            return false;
        }

        if (usuarioRepository.findByEmail(email) != null) {
            return false;
        }

        Optional<RegistroPendiente> pendienteExistente =
                registroRepository.findByUsername(username);

        if (pendienteExistente.isPresent()) {
            registroRepository.delete(pendienteExistente.get());
        }

        RegistroPendiente pendiente =
                new RegistroPendiente(
                        nombre,
                        username,
                        email,
                        passwordService.encriptar(password),
                        LocalDateTime.now().plusMinutes(10)
                );

        registroRepository.save(pendiente);

        return true;
    }

    public boolean confirmarRegistro(String email) {

        Optional<RegistroPendiente> resultado =
                registroRepository.findByEmail(email);

        if (resultado.isEmpty()) {
            return false;
        }

        RegistroPendiente pendiente =
                resultado.get();

        if (LocalDateTime.now()
                .isAfter(pendiente.getExpiracion())) {
            registroRepository.delete(pendiente);
            return false;
        }

        if (usuarioRepository.findByUsername(
                pendiente.getUsername()) != null) {
            return false;
        }

        if (usuarioRepository.findByEmail(
                pendiente.getEmail()) != null) {
            return false;
        }

        Usuario usuario = new Usuario(
                pendiente.getNombre(),
                pendiente.getUsername(),
                pendiente.getPassword(),
                pendiente.getEmail()
        );

        usuarioRepository.save(usuario);


        registroRepository.delete(pendiente);

        return true;
    }
}