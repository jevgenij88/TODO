package de.dreamteam.todolist.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String sendGridFromEmail;

    @Value("${app.url.frontend}")
    private String frontendUrl;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Bestätigen Sie Ihre E-Mail-Adresse";
        String verificationUrl = frontendUrl + "/verify?token=" + token;
        String content = "Bitte bestätigen Sie Ihre E-Mail-Adresse, indem Sie auf den folgenden Link klicken: " +
                "<a href='" + verificationUrl + "'>Bestätigen</a>";

        sendEmail(to, subject, content);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Zurücksetzen des Passworts";
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String content = "Bitte klicken Sie auf den folgenden Link, um Ihr Passwort zurückzusetzen: " +
                "<a href='" + resetUrl + "'>Passwort zurücksetzen</a>";

        sendEmail(to, subject, content);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            Email from = new Email(sendGridFromEmail);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            log.info("Email sent with status code: {}", response.getStatusCode());
        } catch (IOException e) {
            log.error("Error sending email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}