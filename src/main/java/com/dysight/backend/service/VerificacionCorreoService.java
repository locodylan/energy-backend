package com.dysight.backend.service;

import com.dysight.backend.model.VerificacionCorreo;
import com.dysight.backend.repository.VerificacionCorreoRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VerificacionCorreoService {

    private final VerificacionCorreoRepository repository;
    private final EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    public VerificacionCorreoService(
            VerificacionCorreoRepository repository,
            EmailService emailService
    ) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public void enviarCodigo(String email) {

        // Desactivar códigos anteriores de este correo
        Optional<VerificacionCorreo> ultimoCodigo =
                repository.findTopByEmailAndActivoTrueOrderByIdDesc(email);

        if (ultimoCodigo.isPresent()) {
            VerificacionCorreo anterior =
                    ultimoCodigo.get();

            anterior.setActivo(false);
            repository.save(anterior);
        }

        // Generar código aleatorio de 6 dígitos
        String codigo = String.format(
                "%06d",
                random.nextInt(1_000_000)
        );

        // El código será válido durante 10 minutos
        LocalDateTime expiracion =
                LocalDateTime.now().plusMinutes(10);

        VerificacionCorreo verificacion =
                new VerificacionCorreo(
                        email,
                        codigo,
                        expiracion
                );

        repository.save(verificacion);

        // Enviar correo
        emailService.enviarCorreo(
                email,
                "Código de verificación - DySight",
                "Tu código de verificación de DySight es: "
                        + codigo
                        + "\n\n"
                        + "Este código es válido durante 10 minutos."
        );
    }

    public void enviarCodigoRecuperacion(String email) {

    // Desactivar códigos anteriores de este correo
    Optional<VerificacionCorreo> ultimoCodigo =
            repository.findTopByEmailAndActivoTrueOrderByIdDesc(email);

    if (ultimoCodigo.isPresent()) {
        VerificacionCorreo anterior =
                ultimoCodigo.get();

        anterior.setActivo(false);
        repository.save(anterior);
    }

    // Generar código aleatorio de 6 dígitos
    String codigo = String.format(
            "%06d",
            random.nextInt(1_000_000)
    );

    // El código será válido durante 10 minutos
    LocalDateTime expiracion =
            LocalDateTime.now().plusMinutes(10);

    VerificacionCorreo verificacion =
            new VerificacionCorreo(
                    email,
                    codigo,
                    expiracion
            );

    repository.save(verificacion);

    // Enviar correo de recuperación
    emailService.enviarCorreo(
            email,
            "Restablecer contraseña - DySight",
            "Tu código para restablecer tu contraseña de DySight es: "
                    + codigo
                    + "\n\n"
                    + "Este código es válido durante 10 minutos."
                    + "\n\n"
                    + "Si no solicitaste restablecer tu contraseña, "
                    + "puedes ignorar este mensaje."
    );
}

    public boolean verificarCodigo(
            String email,
            String codigo
    ) {

        Optional<VerificacionCorreo> resultado =
                repository.findTopByEmailAndActivoTrueOrderByIdDesc(email);

        if (resultado.isEmpty()) {
            return false;
        }

        VerificacionCorreo verificacion =
                resultado.get();

        // Comprobar si el código está activo
        if (!verificacion.isActivo()) {
            return false;
        }

        // Comprobar si el código ya expiró
        if (LocalDateTime.now()
                .isAfter(verificacion.getExpiracion())) {
            return false;
        }

        // Comprobar que el código coincida
        if (!verificacion.getCodigo().equals(codigo)) {
            return false;
        }

        // El código sigue activo durante sus 10 minutos
        return true;
    }

    public boolean verificarCodigoYMarcar(
        String email,
        String codigo
) {
    Optional<VerificacionCorreo> resultado =
            repository.findTopByEmailAndActivoTrueOrderByIdDesc(email);

    if (resultado.isEmpty()) {
        return false;
    }

    VerificacionCorreo verificacion = resultado.get();

    if (!verificacion.isActivo()) {
        return false;
    }

    if (LocalDateTime.now()
            .isAfter(verificacion.getExpiracion())) {
        return false;
    }

    if (!verificacion.getCodigo().equals(codigo)) {
        return false;
    }

    verificacion.setVerificado(true);
    repository.save(verificacion);

    return true;
}

    public void desactivarCodigo(String email) {

    Optional<VerificacionCorreo> resultado =
            repository.findTopByEmailAndActivoTrueOrderByIdDesc(email);

    if (resultado.isPresent()) {

        VerificacionCorreo verificacion =
                resultado.get();

        verificacion.setActivo(false);

        repository.save(verificacion);
    }
}
}