package com.dysight.backend.controller;

import com.dysight.backend.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/test-email")
    public String testEmail(@RequestParam String destinatario) {

        emailService.enviarCorreo(
                destinatario,
                "Prueba de correo - DySight",
                "Este es un correo de prueba enviado desde el backend de DySight."
        );

        return "Correo enviado correctamente.";
    }
}