package proyecto.nexpay.web.service;

public class EmailNotificationTask implements Runnable {
    private EmailSender emailSender;
    private String subject;
    private String message;
    private String recipientEmail;

    public EmailNotificationTask(EmailSender emailSender, String recipientEmail, String subject, String message) {
        this.emailSender = emailSender;
        this.subject = subject;
        this.message = message;
        this.recipientEmail = recipientEmail;
    }

    @Override
    public void run() {
        try {
            // Enviar el correo
            emailSender.sendNotification(recipientEmail, subject, message);
            System.out.println("Correo enviado a: " + recipientEmail);
        } catch (Exception e) {
            System.out.println("Error al enviar el correo: " + e.getMessage());
        }
    }
}

