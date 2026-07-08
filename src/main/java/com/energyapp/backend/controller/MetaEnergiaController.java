package com.energyapp.backend.controller;

import com.energyapp.backend.model.MetaEnergia;
import com.energyapp.backend.model.Usuario;
import com.energyapp.backend.repository.MetaEnergiaRepository;
import com.energyapp.backend.repository.UsuarioRepository;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/metas")
@CrossOrigin(origins = "*")
public class MetaEnergiaController {

    private final MetaEnergiaRepository repository;
    private final UsuarioRepository usuarioRepository;

    public MetaEnergiaController(
            MetaEnergiaRepository repository,
            UsuarioRepository usuarioRepository
    ) {

        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public MetaEnergia obtenerMeta(

            @RequestParam Long usuarioId
    ) {

        return repository.findByUsuarioId(
                usuarioId
        );
    }

    @PostMapping
    public MetaEnergia guardarMeta(

            @RequestBody MetaEnergia meta,

            @RequestParam Long usuarioId
    ) {

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        MetaEnergia existente =
                repository.findByUsuarioId(
                        usuarioId
                );

        if (existente != null) {

            existente.setLimiteMensual(
                    meta.getLimiteMensual()
            );

            existente.setModoAhorro(
                    meta.isModoAhorro()
            );

            return repository.save(existente);
        }

        meta.setUsuario(usuario);

        return repository.save(meta);
    }

@PutMapping("/modo-ahorro")
public MetaEnergia cambiarModoAhorro(

    @RequestParam Long usuarioId

) {

MetaEnergia meta =
        repository.findByUsuarioId(
                usuarioId
        );

// SI NO EXISTE → CREAR
if (meta == null) {

    Usuario usuario =
            usuarioRepository.findById(usuarioId)
                    .orElse(null);

    meta = new MetaEnergia();

    meta.setUsuario(usuario);

    meta.setModoAhorro(true);

    meta.setLimiteMensual(100.0);

    return repository.save(meta);
}

meta.setModoAhorro(
        !meta.isModoAhorro()
);

return repository.save(meta);

}


}