package com.dysight.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreo(String destinatario, String asunto, String mensaje) {

        SimpleMailMessage correo = new SimpleMailMessage();

        correo.setFrom("noreply.dysight@gmail.com");
        correo.setTo(destinatario);
        correo.setSubject(asunto);
        correo.setText(mensaje);

        mailSender.send(correo);
    }
}