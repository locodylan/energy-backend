package com.dysight.backend.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dysight.backend.model.Usuario;
import com.dysight.backend.model.VerificacionCorreo;
import com.dysight.backend.repository.UsuarioRepository;
import com.dysight.backend.repository.VerificacionCorreoRepository;
import com.dysight.backend.service.PasswordService;
import com.dysight.backend.service.VerificacionCorreoService;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final PasswordService passwordService;
    private final VerificacionCorreoService verificacionCorreoService;
    private final VerificacionCorreoRepository verificacionCorreoRepository;

    public UsuarioController(
            UsuarioRepository repository,
            PasswordService passwordService,
            VerificacionCorreoService verificacionCorreoService,
            VerificacionCorreoRepository verificacionCorreoRepository
    ) {
        this.repository = repository;
        this.passwordService = passwordService;
        this.verificacionCorreoService = verificacionCorreoService;
        this.verificacionCorreoRepository = verificacionCorreoRepository;
    }

    // =========================================================
    // REGISTRO
    // =========================================================

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {

        if (repository.findByUsername(usuario.getUsername()) != null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "El nombre de usuario ya está registrado."
                    )
            );
        }

        usuario.setPassword(
                passwordService.encriptar(usuario.getPassword())
        );

        // El PIN no se configura durante el registro.
        usuario.setPin(null);

        Usuario guardado = repository.save(usuario);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mensaje", "Usuario registrado correctamente.",
                        "usuarioId", guardado.getId()
                )
        );
    }

    // =========================================================
    // LOGIN CON USUARIO Y CONTRASEÑA
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> datos
    ) {

        String username = datos.get("username");
        String password = datos.get("password");

        Usuario usuario = repository.findByUsername(username);

        if (usuario == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Usuario o contraseña incorrectos."
                    )
            );
        }

        boolean passwordCorrecta = passwordService.verificar(
                password,
                usuario.getPassword()
        );

        if (!passwordCorrecta) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Usuario o contraseña incorrectos."
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mensaje", "Inicio de sesión correcto.",
                        "usuarioId", usuario.getId(),
                        "username", usuario.getUsername(),
                        "nombre", usuario.getNombre()
                )
        );
    }

    // =========================================================
    // CONFIGURAR PIN POR PRIMERA VEZ
    // =========================================================

    @PostMapping("/configurar-pin")
    public ResponseEntity<?> configurarPin(
            @RequestBody Map<String, String> datos
    ) {

        String username = datos.get("username");
        String password = datos.get("password");
        String pin = datos.get("pin");

        if (username == null ||
                password == null ||
                pin == null) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Todos los campos son obligatorios."
                    )
            );
        }

        // El PIN debe tener exactamente 6 dígitos.
        if (!pin.matches("\\d{6}")) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "El PIN debe contener exactamente 6 dígitos."
                    )
            );
        }

        Usuario usuario = repository.findByUsername(username);

        if (usuario == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Usuario no encontrado."
                    )
            );
        }

        // Verificar contraseña actual.
        boolean passwordCorrecta = passwordService.verificar(
                password,
                usuario.getPassword()
        );

        if (!passwordCorrecta) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "La contraseña actual es incorrecta."
                    )
            );
        }

        // No permitir configurar nuevamente un PIN existente.
        if (usuario.getPin() != null &&
                !usuario.getPin().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "El usuario ya tiene un PIN configurado."
                    )
            );
        }

        // Guardar PIN como hash BCrypt.
        usuario.setPin(
                passwordService.encriptar(pin)
        );

        repository.save(usuario);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mensaje", "PIN configurado correctamente."
                )
        );
    }

    // =========================================================
// CONSULTAR SI EL USUARIO TIENE PIN CONFIGURADO
// =========================================================

@GetMapping("/tiene-pin/{username}")
public ResponseEntity<?> tienePin(
        @PathVariable String username
) {

    Usuario usuario = repository.findByUsername(username);

    if (usuario == null) {
        return ResponseEntity.status(404).body(
                Map.of(
                        "success", false,
                        "mensaje", "Usuario no encontrado."
                )
        );
    }

    boolean tienePin = usuario.getPin() != null
            && !usuario.getPin().isBlank();

    return ResponseEntity.ok(
            Map.of(
                    "success", true,
                    "tienePin", tienePin
            )
    );
}

    // =========================================================
    // LOGIN CON PIN
    // =========================================================

    @PostMapping("/login-pin")
    public ResponseEntity<?> loginPin(
            @RequestBody Map<String, String> datos
    ) {

        String username = datos.get("username");
        String pin = datos.get("pin");

        if (username == null || pin == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Usuario y PIN son obligatorios."
                    )
            );
        }

        if (!pin.matches("\\d{6}")) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "El PIN debe contener exactamente 6 dígitos."
                    )
            );
        }

        Usuario usuario = repository.findByUsername(username);

        if (usuario == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Usuario o PIN incorrectos."
                    )
            );
        }

        // Comprobar que tenga PIN configurado.
        if (usuario.getPin() == null ||
                usuario.getPin().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Este usuario no tiene un PIN configurado."
                    )
            );
        }

        boolean pinCorrecto = passwordService.verificar(
                pin,
                usuario.getPin()
        );

        if (!pinCorrecto) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Usuario o PIN incorrectos."
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mensaje", "Inicio de sesión correcto.",
                        "usuarioId", usuario.getId(),
                        "username", usuario.getUsername(),
                        "nombre", usuario.getNombre()
                )
        );
    }

    // =========================================================
    // CAMBIAR PIN
    // =========================================================

    @PutMapping("/cambiar-pin")
    public ResponseEntity<?> cambiarPin(
            @RequestBody Map<String, String> datos
    ) {

        String username = datos.get("username");
        String pinActual = datos.get("pinActual");
        String pinNuevo = datos.get("pinNuevo");

        if (username == null ||
                pinActual == null ||
                pinNuevo == null) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Todos los campos son obligatorios."
                    )
            );
        }

        if (!pinActual.matches("\\d{6}") ||
                !pinNuevo.matches("\\d{6}")) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "El PIN debe contener exactamente 6 dígitos."
                    )
            );
        }

        Usuario usuario = repository.findByUsername(username);

        if (usuario == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Usuario no encontrado."
                    )
            );
        }

        if (usuario.getPin() == null ||
                usuario.getPin().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Este usuario no tiene un PIN configurado."
                    )
            );
        }

        boolean pinActualCorrecto = passwordService.verificar(
                pinActual,
                usuario.getPin()
        );

        if (!pinActualCorrecto) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "El PIN actual es incorrecto."
                    )
            );
        }

        // Evitar usar el mismo PIN.
        if (passwordService.verificar(
                pinNuevo,
                usuario.getPin()
        )) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "El nuevo PIN debe ser diferente al actual."
                    )
            );
        }

        usuario.setPin(
                passwordService.encriptar(pinNuevo)
        );

        repository.save(usuario);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mensaje", "PIN cambiado correctamente."
                )
        );
    }

    // =========================================================
    // RECUPERAR CONTRASEÑA
    // =========================================================

    @PostMapping("/recuperar-password")
    public ResponseEntity<?> recuperarPassword(
            @RequestBody Map<String, String> datos
    ) {

        String email = datos.get("email");

        Usuario usuario = repository.findByEmail(email);

        if (usuario == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "No existe una cuenta asociada a ese correo."
                    )
            );
        }

        verificacionCorreoService.enviarCodigo(email);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mensaje", "Código de verificación enviado al correo."
                )
        );
    }

    // =========================================================
    // VERIFICAR CÓDIGO DE RECUPERACIÓN
    // =========================================================

    @PostMapping("/verificar-recuperacion")
    public ResponseEntity<?> verificarRecuperacion(
            @RequestBody Map<String, String> datos
    ) {

        String email = datos.get("email");
        String codigo = datos.get("codigo");

        Optional<VerificacionCorreo> optionalVerificacion =
                verificacionCorreoRepository
                        .findTopByEmailAndActivoTrueOrderByIdDesc(email);

        if (optionalVerificacion.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "No existe una solicitud de recuperación activa."
                    )
            );
        }

        VerificacionCorreo verificacion =
                optionalVerificacion.get();

        if (!verificacion.getCodigo().equals(codigo)) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "El código de verificación es incorrecto."
                    )
            );
        }

        if (verificacion.getExpiracion()
                .isBefore(LocalDateTime.now())) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "El código de verificación ha expirado."
                    )
            );
        }

        verificacion.setVerificado(true);
        verificacionCorreoRepository.save(verificacion);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mensaje", "Código verificado correctamente."
                )
        );
    }

    // =========================================================
    // RESTABLECER CONTRASEÑA
    // =========================================================

    @PutMapping("/restablecer-password")
    public ResponseEntity<?> restablecerPassword(
            @RequestBody Map<String, String> datos
    ) {

        String email = datos.get("email");
        String codigo = datos.get("codigo");
        String nuevaPassword = datos.get("nuevaPassword");

        Usuario usuario = repository.findByEmail(email);

        if (usuario == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Usuario no encontrado."
                    )
            );
        }

        Optional<VerificacionCorreo> optionalVerificacion =
                verificacionCorreoRepository
                        .findTopByEmailOrderByIdDesc(email);

        if (optionalVerificacion.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "No existe una solicitud de recuperación."
                    )
            );
        }

        VerificacionCorreo verificacion =
                optionalVerificacion.get();

        if (!verificacion.getCodigo().equals(codigo)) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Código de verificación inválido."
                    )
            );
        }

        if (verificacion.getExpiracion()
                .isBefore(LocalDateTime.now())) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "El código de verificación ha expirado."
                    )
            );
        }

        usuario.setPassword(
                passwordService.encriptar(nuevaPassword)
        );

        repository.save(usuario);

        // Invalidar el código utilizado.
        verificacion.setActivo(false);
        verificacionCorreoRepository.save(verificacion);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mensaje", "Contraseña restablecida correctamente."
                )
        );
    }

    // =========================================================
    // CAMBIAR CONTRASEÑA
    // =========================================================

    @PutMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(
            @RequestBody Map<String, String> datos
    ) {

        String username = datos.get("username");
        String nuevaPassword = datos.get("nuevaPassword");

        Usuario usuario = repository.findByUsername(username);

        if (usuario == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "mensaje", "Usuario no encontrado."
                    )
            );
        }

        usuario.setPassword(
                passwordService.encriptar(nuevaPassword)
        );

        repository.save(usuario);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mensaje", "Contraseña cambiada correctamente."
                )
        );
    }
}