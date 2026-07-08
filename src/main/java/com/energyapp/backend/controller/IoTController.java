package com.energyapp.backend.controller;

import com.energyapp.backend.model.DispositivoIoT;
import com.energyapp.backend.model.Usuario;
import com.energyapp.backend.repository.DispositivoIoTRepository;
import com.energyapp.backend.repository.UsuarioRepository;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/iot")
@CrossOrigin(origins="*")
public class IoTController {

private final DispositivoIoTRepository repository;
private final UsuarioRepository usuarioRepository;

public IoTController(
        DispositivoIoTRepository repository,
        UsuarioRepository usuarioRepository
) {
    this.repository = repository;
    this.usuarioRepository = usuarioRepository;
}

@GetMapping
public List<DispositivoIoT> listar(
        @RequestParam Long usuarioId
) {

    return repository.findByUsuarioId(
            usuarioId
    );
}

@PostMapping("/crear")
public DispositivoIoT crear(
        @RequestBody DispositivoIoT dispositivo,
        @RequestParam Long usuarioId
) {

    Usuario usuario =
            usuarioRepository.findById(usuarioId)
                    .orElse(null);

    dispositivo.setUsuario(usuario);

    return repository.save(dispositivo);
}

@PutMapping("/{id}")
public DispositivoIoT cambiarEstado(
        @PathVariable Long id
) {

    DispositivoIoT dispositivo =
            repository.findById(id)
                    .orElse(null);

    if (dispositivo != null) {

        dispositivo.setEncendido(
                !dispositivo.isEncendido()
        );

        return repository.save(dispositivo);
    }

    return null;
}
@GetMapping("/resumen")
public Map<String, Integer> resumen(
@RequestParam Long usuarioId
) {

List<DispositivoIoT> lista =
        repository.findByUsuarioId(
                usuarioId
        );

int encendidos = 0;
int apagados = 0;

for (DispositivoIoT d : lista) {

    if (d.isEncendido()) {
        encendidos++;
    } else {
        apagados++;
    }
}

Map<String, Integer> resultado =
        new HashMap<>();

resultado.put(
        "encendidos",
        encendidos
);

resultado.put(
        "apagados",
        apagados
);

resultado.put(
        "total",
        lista.size()
);

return resultado;

}
@PutMapping("/toggle")
public DispositivoIoT toggle(

        @RequestParam Long usuarioId,
        @RequestParam String nombre
) {

    DispositivoIoT dispositivo =

            repository.findByNombreAndUsuarioId(
                    nombre,
                    usuarioId
            );

    if (dispositivo == null) {
        return null;
    }

    dispositivo.setEncendido(
            !dispositivo.isEncendido()
    );

    return repository.save(dispositivo);
}

}
