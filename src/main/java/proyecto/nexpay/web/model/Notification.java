package proyecto.nexpay.web.model;

import java.time.LocalDateTime;

public class Notification {
        private String message;
        private LocalDateTime timestamp;
        private NotificationType type;

        public Notification(String message, NotificationType type) {
            this.message = message;
            this.timestamp = LocalDateTime.now();
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public NotificationType getType() {
            return type;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

