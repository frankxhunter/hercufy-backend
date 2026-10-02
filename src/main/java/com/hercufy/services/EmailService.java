package com.hercufy.services;

import com.hercufy.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    /**
     * Envia el correo de confirmacion. Si el SMTP todavia no esta configurado (caso
     * normal en desarrollo, antes de conectar un proveedor real), el fallo se
     * registra como aviso en el log en vez de romper el registro del usuario: el
     * token de verificacion ya ha quedado guardado en base de datos y se puede leer
     * ahi para confirmar la cuenta a mano mientras tanto.
     */
    public void sendEmailConfirmation(User user, String token, String baseUrlFront) {
        String confirmUrl = baseUrlFront + "/confirm-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        if (!fromAddress.isEmpty()) {
            message.setFrom(fromAddress);
        }
        message.setSubject("Confirma tu correo en Hercufy");
        message.setText("Hola,\n\nPor favor, haz clic en el siguiente enlace para confirmar tu correo:\n"
                + confirmUrl + "\n\nSi no has solicitado esta cuenta, puedes ignorar este mensaje.");

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("No se pudo enviar el correo de confirmacion a {} (SMTP sin configurar todavia?). "
                    + "Token de verificacion: {}", user.getEmail(), token);
        }
    }
}
