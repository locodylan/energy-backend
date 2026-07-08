package com.energyapp.backend.controller;

import com.energyapp.backend.model.Usuario;
import com.energyapp.backend.repository.UsuarioRepository;
import com.energyapp.backend.model.MetaEnergia;
import com.energyapp.backend.repository.MetaEnergiaRepository;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final MetaEnergiaRepository metaRepository;

    public UsuarioController(
            UsuarioRepository repository, MetaEnergiaRepository metaRepository
    ) {
        this.repository = repository;
        this.metaRepository = metaRepository;
    }

    @PostMapping("/registro")
    public Map<String, Object> registro(
            @RequestBody Usuario usuario
    ) {

        Map<String, Object> respuesta =
                new HashMap<>();

        Usuario existe =
                repository.findByUsername(
                        usuario.getUsername()
                );

        if (existe != null) {

            respuesta.put(
                    "success",
                    false
            );

            respuesta.put(
                    "mensaje",
                    "El usuario ya existe"
            );

            return respuesta;
        }

        Usuario nuevoUsuario =
repository.save(usuario);

// CREAR CONFIGURACIÓN INICIAL
MetaEnergia meta =
new MetaEnergia();

meta.setUsuario(nuevoUsuario);

meta.setModoAhorro(false);

// límite inicial
meta.setLimiteMensual(100.0);

metaRepository.save(meta);


        respuesta.put(
                "success",
                true
        );

        respuesta.put(
                "mensaje",
                "Usuario registrado"
        );

        return respuesta;
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody Usuario usuario
    ) {

        Map<String, Object> respuesta =
                new HashMap<>();

        Usuario existe =
                repository.findByUsername(
                        usuario.getUsername()
                );

        if (existe == null) {

            respuesta.put(
                    "success",
                    false
            );

            respuesta.put(
                    "mensaje",
                    "Usuario no encontrado"
            );

            return respuesta;
        }

        if (!existe.getPassword().equals(
                usuario.getPassword()
        )) {

            respuesta.put(
                    "success",
                    false
            );

            respuesta.put(
                    "mensaje",
                    "Contraseña incorrecta"
            );

            return respuesta;
        }

respuesta.put(
        "success",
        true
);

respuesta.put(
        "mensaje",
        "Login correcto"
);

respuesta.put(
        "usuarioId",
        existe.getId()
);

respuesta.put(
        "username",
        existe.getUsername()
);

respuesta.put(
        "nombre",
        existe.getNombre()
);

return respuesta;
    }
    @PutMapping("/cambiar-password")
public Map<String, Object> cambiarPassword(

        @RequestBody Usuario usuario
) {

    Map<String, Object> respuesta =
            new HashMap<>();

    Usuario existe =
            repository.findByUsername(
                    usuario.getUsername()
            );

    if (existe == null) {

        respuesta.put(
                "success",
                false
        );

        respuesta.put(
                "mensaje",
                "Usuario no encontrado"
        );

        return respuesta;
    }

    existe.setPassword(
            usuario.getPassword()
    );

    repository.save(existe);

    respuesta.put(
            "success",
            true
    );
respuesta.put("id", existe.getId());

respuesta.put(
    "username",
    existe.getUsername()
);
    respuesta.put(
            "mensaje",
            "Contraseña actualizada"
    );

    return respuesta;
}
}