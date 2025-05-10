package proyecto.nexpay.web.service;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    private final String host = "smtp.gmail.com";
    private final String port = "587";
    private final String remitente = "lauram.cardenass@uqvirtual.edu.co";
    private final String password = "bptomvmocohfufai";

    public void sendNotification(String destinatario, String subject, String messageBody) {
        TrustAllCertificates.disableSSL();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", host);

        javax.mail.Session session = javax.mail.Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, password);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(subject);
            message.setText(messageBody);
            Transport.send(message);
            System.out.println("Correo enviado satisfactoriamente.");

        } catch (MessagingException e) {
            System.out.println("Error al enviar el correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}




