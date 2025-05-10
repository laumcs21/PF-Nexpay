package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.CircularLinkedList;
import proyecto.nexpay.web.model.*;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationManager {

    private CircularLinkedList<Notification> notifications;
    private EmailSender emailSender;
    private ExecutorService executorService;
    private Nexpay nexpay;

    public NotificationManager(Nexpay nexpay) {
        this.notifications = new CircularLinkedList<>();
        this.emailSender = new EmailSender();
        this.nexpay = nexpay;
        this.executorService = Executors.newCachedThreadPool();
    }

    public void checkLowBalance() {

        for (Account account : nexpay.getAccounts()) {
            if (account.getBalance() <= 20000) {
                Notification lowBalanceNotification = new Notification("Alerta: Saldo bajo en tu cuenta.", NotificationType.LOW_BALANCE);
                notifications.add(lowBalanceNotification);
                System.out.println("Notificación de saldo bajo: " + lowBalanceNotification.getMessage());

                String subject = "Alerta de saldo bajo";
                String message = "Estimado usuario, su saldo actual es de " + account.getBalance() + ". Por favor, realice un depósito si es necesario.";
                String UserID = account.getUserId();
                User user = nexpay.getUserCRUD().safeRead(UserID);  // Accedemos al CRUD de usuario desde la instancia Nexpay
                if (user != null) {
                    EmailNotificationTask task = new EmailNotificationTask(emailSender, user.getEmail(), subject, message);
                    try {
                        executorService.submit(task); // Enviar la tarea al ExecutorService (hilo)
                    } catch (Exception e) {
                        System.err.println("Error al enviar la notificación de saldo bajo: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Usuario no encontrado con ID: " + UserID);
                }
            }
        }
    }

    public void checkScheduledTransactions() {
        LocalDateTime now = LocalDateTime.now();
        for (ScheduledTransaction scheduledTransaction : nexpay.getSManager().getScheduledTransactions()) {
            if (scheduledTransaction.getScheduledDate().isEqual(now) || scheduledTransaction.getScheduledDate().isBefore(now)) {
                Notification scheduledTransactionNotification = new Notification(
                        "Recordatorio: Tienes una transacción programada para hoy.", NotificationType.SCHEDULED_TRANSACTION);
                notifications.add(scheduledTransactionNotification);
                System.out.println("Notificación de transacción programada: " + scheduledTransactionNotification.getMessage());

                String subject = "Recordatorio de transacción programada";
                String message = "Estimado usuario, le recordamos que tiene una transacción programada para hoy: " + scheduledTransaction.toString();
                String UserID = scheduledTransaction.getTransaction().getUserId();
                User user = nexpay.getUserCRUD().safeRead(UserID);  // Accedemos al CRUD de usuario desde Nexpay
                if (user != null) {
                    EmailNotificationTask task = new EmailNotificationTask(emailSender, user.getEmail(), subject, message);
                    try {
                        executorService.submit(task); // Enviar la tarea al ExecutorService (hilo)
                    } catch (Exception e) {
                        System.err.println("Error al enviar la notificación de transacción programada: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Usuario no encontrado con ID: " + UserID);
                }
            }
        }
    }

    public void checkUndoTransaction() {
        if (nexpay.getTManager().isUndoTransaction()) {
            Transaction lastTransaction = nexpay.getTManager().getRevertedTransactions().peek();
            if (lastTransaction != null) {
                System.out.println("Transacción revertida: " + lastTransaction.getId());

                String subject = "Reversión de Transacción";
                String message = "Estimado usuario, su transacción de tipo '" + lastTransaction.getType() +
                        "' con el código '" + lastTransaction.getId() + "' ha sido revertida exitosamente. " +
                        "El monto de la transacción de " + lastTransaction.getAmount() + " ha sido devuelto a su cuenta.";

                User user = nexpay.getUserCRUD().safeRead(lastTransaction.getUserId());
                if (user != null) {
                    emailSender.sendNotification(user.getEmail(), subject, message);
                    System.out.println("Notificación de reversión enviada a: " + user.getEmail());
                } else {
                    System.out.println("Usuario no encontrado con ID: " + lastTransaction.getUserId());
                }
            } else {
                System.out.println("No se encontró transacción revertida.");
            }
        }
    }

    public void processNotifications() {
        while (!notifications.isEmpty()) {
            Notification notification = notifications.remove();
            System.out.println("Enviando notificación: " + notification.getMessage());
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}








