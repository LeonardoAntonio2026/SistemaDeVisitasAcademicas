package com.example.demo.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

public class EmailSender {

    public static void sendMail(String to, String subject, String body) {
        // 1. Configuración del servidor SMTP (Actualizado para TLS moderno)
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true"); // Requerir TLS seguro obligatoriamente
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Solución al problema de TLS Handshake en Java moderno
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Timeouts para evitar congelamientos eternos si falla la red
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        // Variables temporales para la lógica
        String userTemp = System.getenv("SMTP_USER");
        String passTemp = System.getenv("SMTP_PASS");

        if (userTemp == null || passTemp == null) {
            System.err.println("Advertencia: Variables de entorno no encontradas. Buscando en credentials.properties...");
            Properties creds = new Properties();
            try (InputStream is = EmailSender.class.getClassLoader().getResourceAsStream("credentials.properties")) {
                if (is == null) {
                    throw new RuntimeException("No se encontró el archivo credentials.properties ni las variables de entorno.");
                }

                // Solución para respetar la codificación ISO-8859-1 del archivo
                try (java.io.InputStreamReader reader = new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.ISO_8859_1)) {
                    creds.load(reader);
                }

                userTemp = creds.getProperty("smtp.user");
                passTemp = creds.getProperty("smtp.pass");
            } catch (Exception e) {
                throw new RuntimeException("Error al cargar las credenciales: " + e.getMessage());
            }
        }

        // 2. Credenciales DEFINITIVAS y FINALES
        final String usuario = userTemp;
        final String contrasena = passTemp;

        // 3. Crear la sesión
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, contrasena);
            }
        });

        try {
            // 4. Crear el mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(usuario));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            // 5. Enviar
            Transport.send(message);
            System.out.println("¡Correo enviado con éxito a: " + to + "!");

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage());
        }
    }
}