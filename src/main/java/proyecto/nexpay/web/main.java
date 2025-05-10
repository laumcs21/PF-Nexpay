package proyecto.nexpay.web;

import proyecto.nexpay.web.model.Nexpay;
import proyecto.nexpay.web.service.*;


public class main {

    public static void main(String[] args) {
        EmailSender emailSender = new EmailSender();
        Nexpay nexpay = Nexpay.getInstance();
        NotificationManager notifi = new NotificationManager(nexpay);

        notifi.checkUndoTransaction();
        String destinatario = "laumcs21@gmail.com";
        String subject = "Correo de prueba";
        String messageBody = "Este es un correo de prueba para verificar el EmailSender.";

        emailSender.sendNotification(destinatario, subject, messageBody);

        System.out.println("Correo enviado a: " + destinatario);
    }
}

