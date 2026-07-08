package com.energyapp.backend.controller;

import com.energyapp.backend.model.ConsumoEnergia;
import com.energyapp.backend.repository.ConsumoRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ConsumoRepository repository;

    public ReporteController(
            ConsumoRepository repository
    ) {
        this.repository = repository;
    }

   @GetMapping("/pdf")
public ResponseEntity<byte[]> generarPDF(

        @RequestParam Long usuarioId

) {

    try {

        List<ConsumoEnergia> consumos =
        repository.findByUsuarioId(usuarioId);

        double total = 0;

        for (ConsumoEnergia c : consumos) {
            total += c.getConsumo();
        }

        double promedio =
                consumos.isEmpty()
                ? 0
                : total / consumos.size();

        double prediccionIA =
                promedio * 1.1;

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        Document document =
                new Document();

        PdfWriter.getInstance(document, out);

        document.open();

        document.add(
                new Paragraph(
                        "========== ENERGY IA REPORT =========="
                )
        );

        document.add(
                new Paragraph(
                        "Fecha del reporte: "
                        + java.time.LocalDate.now()
                )
        );

        document.add(
                new Paragraph(" ")
        );

        document.add(
                new Paragraph(
                        "===== RESUMEN ENERGETICO ====="
                )
        );

        document.add(
                new Paragraph(
                        "Consumo Total: "
                        + total + " kWh"
                )
        );

        document.add(
                new Paragraph(
                        "Promedio: "
                        + promedio + " kWh"
                )
        );

        document.add(
                new Paragraph(
                        "Prediccion IA: "
                        + prediccionIA + " kWh"
                )
        );

        document.add(
                new Paragraph(" ")
        );

        document.add(
                new Paragraph(
                        "===== DISPOSITIVOS ====="
                )
        );

        for (ConsumoEnergia c : consumos) {

            document.add(
                    new Paragraph(
                            c.getDispositivo()
                            + " -> "
                            + c.getConsumo()
                            + " kWh"
                    )
            );
        }

        document.add(
                new Paragraph(" ")
        );

        document.add(
                new Paragraph(
                        "===== ALERTAS ====="
                )
        );

        for (ConsumoEnergia c : consumos) {

            if (c.getConsumo() > promedio * 1.1) {

                document.add(
                        new Paragraph(
                                "Alerta: "
                                + c.getDispositivo()
                                + " tiene consumo elevado"
                        )
                );
            }
        }

        document.add(
                new Paragraph(" ")
        );

        document.add(
                new Paragraph(
                        "===== RECOMENDACIONES IA ====="
                )
        );

        for (ConsumoEnergia c : consumos) {

            if (c.getConsumo() > 5) {

                document.add(
                        new Paragraph(
                                "Reducir uso de "
                                + c.getDispositivo()
                        )
                );

            } else {

                document.add(
                        new Paragraph(
                                "Consumo estable en "
                                + c.getDispositivo()
                        )
                );
            }
        }

        document.add(
                new Paragraph(" ")
        );

        document.add(
                new Paragraph(
                        "===== ANALISIS IA ====="
                )
        );

        if (prediccionIA > promedio) {

            document.add(
                    new Paragraph(
                            "La IA detecta tendencia de aumento de consumo."
                    )
            );

        } else {

            document.add(
                    new Paragraph(
                            "La IA detecta consumo estable."
                    )
            );
        }

        document.close();

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte.pdf"
                )

                .contentType(
                        MediaType.APPLICATION_PDF
                )

                .body(out.toByteArray());

    } catch (Exception e) {

        e.printStackTrace();

        return ResponseEntity.internalServerError()
                .build();
    }
}
}