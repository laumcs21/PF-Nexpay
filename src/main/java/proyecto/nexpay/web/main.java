package proyecto.nexpay.web;

import proyecto.nexpay.web.persistence.TransactionPersistence;
import proyecto.nexpay.web.model.Transaction;
import proyecto.nexpay.web.model.Nexpay;
import proyecto.nexpay.web.model.TransactionType;
import proyecto.nexpay.web.datastructures.DoubleLinkedList;
import proyecto.nexpay.web.service.*;


public class main {

    public static void main(String[] args) {
        EmailSender emailSender = new EmailSender();

        String destinatario = "laumcs21@gmail.com";
        String subject = "Correo de prueba";
        String messageBody = "Este es un correo de prueba para verificar el EmailSender.";

        emailSender.sendNotification(destinatario, subject, messageBody);

        System.out.println("Correo enviado a: " + destinatario);
    }
}

