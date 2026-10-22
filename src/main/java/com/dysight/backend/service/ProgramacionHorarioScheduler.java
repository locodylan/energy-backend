package com.dysight.backend.service;

import com.dysight.backend.model.DiaSemana;
import com.dysight.backend.model.DispositivoIoT;
import com.dysight.backend.model.EstadoProgramacion;
import com.dysight.backend.model.ProgramacionHorario;
import com.dysight.backend.repository.ProgramacionHorarioRepository;
import com.dysight.backend.repository.DispositivoIoTRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class ProgramacionHorarioScheduler {

    private final ProgramacionHorarioRepository programacionRepository;
    private final DispositivoIoTRepository dispositivoRepository;
    private final ShellyService shellyService;

    public ProgramacionHorarioScheduler(
            ProgramacionHorarioRepository programacionRepository,
            DispositivoIoTRepository dispositivoRepository,
            ShellyService shellyService) {

        this.programacionRepository = programacionRepository;
        this.dispositivoRepository = dispositivoRepository;
        this.shellyService = shellyService;
    }

    @Scheduled(fixedRate = 5000)
    public void revisarProgramacionesHorario() {

        List<ProgramacionHorario> programaciones =
                programacionRepository.findByEstadoConDias(
                        EstadoProgramacion.ACTIVA
                );

        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = ahora.toLocalTime();

        for (ProgramacionHorario programacion : programaciones) {

            DispositivoIoT dispositivo =
                    programacion.getDispositivo();

            boolean debeEstarEncendido =
                    estaDentroDelHorario(
                            programacion,
                            ahora.getDayOfWeek(),
                            horaActual
                    );

            sincronizarDispositivo(
                    dispositivo,
                    debeEstarEncendido
            );
        }
    }

    private boolean estaDentroDelHorario(
            ProgramacionHorario programacion,
            DayOfWeek diaActual,
            LocalTime horaActual) {

        DiaSemana dia = convertirDia(diaActual);

        LocalTime inicio = programacion.getHoraInicio();
        LocalTime fin = programacion.getHoraFin();

        /*
         * Horario normal:
         *
         * 08:00 -> 12:00
         *
         * El dispositivo debe estar encendido cuando:
         *
         * 08:00 <= hora < 12:00
         */
        if (inicio.isBefore(fin)) {

            return programacion.getDias().contains(dia)
                    && !horaActual.isBefore(inicio)
                    && horaActual.isBefore(fin);
        }

        /*
         * Horario que cruza medianoche:
         *
         * 22:00 -> 06:00
         *
         * Parte 1:
         * día seleccionado desde las 22:00 hasta medianoche.
         */
        if (programacion.getDias().contains(dia)
                && !horaActual.isBefore(inicio)) {

            return true;
        }

        /*
         * Parte 2:
         * día siguiente desde 00:00 hasta las 06:00.
         */
        DiaSemana diaAnterior =
                convertirDia(diaActual.minus(1));

        return programacion.getDias().contains(diaAnterior)
                && horaActual.isBefore(fin);
    }

    private void sincronizarDispositivo(
            DispositivoIoT dispositivo,
            boolean debeEstarEncendido) {

        try {

            /*
             * Consultamos el estado REAL del Shelly.
             */
            Map<String, Object> estadoShelly =
                    shellyService.obtenerEstado(dispositivo);

            Boolean estadoActual =
                    (Boolean) estadoShelly.get("output");

            if (estadoActual == null) {
                System.out.println(
                        "No se pudo obtener el estado del Shelly: "
                                + dispositivo.getNombre()
                );
                return;
            }

            /*
             * Sincronizamos PostgreSQL con el estado
             * real del dispositivo.
             */
            if (dispositivo.isEncendido() != estadoActual) {

                dispositivo.setEncendido(estadoActual);
                dispositivoRepository.save(dispositivo);
            }

            /*
             * Si el estado real ya coincide con lo que
             * establece la programación, no hacemos nada.
             */
            if (estadoActual == debeEstarEncendido) {
                return;
            }

            /*
             * El estado real no coincide con la programación.
             * Cambiamos físicamente el Shelly.
             */
            shellyService.cambiarEstado(
                    dispositivo,
                    debeEstarEncendido
            );

            /*
             * Actualizamos también PostgreSQL.
             */
            dispositivo.setEncendido(debeEstarEncendido);
            dispositivoRepository.save(dispositivo);

            System.out.println(
                    "Programación por horario: "
                            + dispositivo.getNombre()
                            + " -> "
                            + (debeEstarEncendido
                            ? "ENCENDIDO"
                            : "APAGADO")
            );

        } catch (Exception e) {

            /*
             * Si el Shelly no responde, no modificamos
             * el estado de la base de datos.
             */
            System.out.println(
                    "No se pudo sincronizar el dispositivo "
                            + dispositivo.getNombre()
                            + ": "
                            + e.getMessage()
            );
        }
    }

    private DiaSemana convertirDia(DayOfWeek dayOfWeek) {

        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }
}