package OrderManagement;

import Enums.Fields.NotificationFields;
import Enums.Types.*;
import Services.Parsers;
import Services.DBConnect;
import Enums.*;
import Services.ServerManager;
import UserManagement.Client;
import org.javatuples.Pair;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Notifications {
    private String notificationId;
    private String notificationRecipient;
    private Client sender;
    private Client recipient;
    private Order order;
    private LocalDateTime sentDateTime;
    private String message;

    public Notifications(Order order) {
        setSender(order.getSender());
        setRecipient(order.getRecipient());
        setOrder(order);
    }

    public String getNotificationId() {
        return this.notificationId;
    }
    public String getNotificationRecipient() {
        return notificationRecipient;
    }
    public String getSenderId() {
        return this.sender.getClientId();
    }
    public String getRecipientId() {
        return this.recipient.getClientId();
    }
    public String getOrderId() {
        return this.order.getOrderId();
    }
    public LocalDateTime getSentDateTime() {
        return this.sentDateTime;
    }
    public String getMessage() {
        return this.message;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
    public void setNotificationRecipient(String notificationRecipient) {
        this.notificationRecipient = notificationRecipient;
    }
    public void setSender(Client sender) throws IllegalArgumentException {
        if (Integer.parseInt(sender.getClientId()) < 1)
            throw new IllegalArgumentException("Non-existing sender, order ID is invalid.");
        this.sender = sender;
    }
    public void setRecipient(Client recipient) throws IllegalArgumentException {
        if (Integer.parseInt(recipient.getClientId()) < 1)
            throw new IllegalArgumentException("Non-existing recipient, order ID is invalid.");
        this.recipient = recipient;
    }
    public void setOrder(Order order) throws IllegalArgumentException {
        if (Integer.parseInt(order.getOrderId()) < 1)
            throw new IllegalArgumentException("Non-existing order, order ID is invalid.");
        this.order = order;
    }
    public void setSentDateTime(LocalDateTime sentDateTime) throws IllegalArgumentException {
        if (sentDateTime.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Incorrect order Date/Time provided.");
        this.sentDateTime = sentDateTime.truncatedTo(ChronoUnit.SECONDS);
    }
    public void setMessage(String message) throws IllegalArgumentException {
        if (message.length() > 500)
            throw new IllegalArgumentException("Message length is over 500 symbols.");
        this.message = message;
    }

    /**
     * Send notification to the sender
     * @param addressee value from NotificationAddressee enum
     * @param type value from NotificationType enum
     */
    public void notify(NotificationAddressee addressee, NotificationType type) {
        try {
            String message = buildNotification(addressee, type);
            switch (addressee){
                case SENDER -> setNotificationRecipient(getSenderId());
                case RECIPIENT -> setNotificationRecipient(getRecipientId());
            }
            CommunicationCode code = null;
            switch (type) {
                case Sent -> code = CommunicationCode.OrderSent;
                case Delivered -> code = CommunicationCode.OrderDelivered;
            }
            setSentDateTime(LocalDateTime.now());
            setMessage(message);
            String id = DBConnect.insertNotification(this);
            System.out.println(id);
            setNotificationId(id);
            if (code != null)
                message = Parsers.parseMessageToClient(code, new ArrayList<>(Arrays.asList(getNotificationRecipient(), message)));
            sendNotification(message);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds the notification message
     * @param addressee a value from NotificationAddressee enum
     * @param type value from NotificationType enum
     * @return notification message as String
     * @throws IllegalArgumentException is thrown in case we try to send the recipient notifications on events other that Sent or Delivered
     */
    private String buildNotification(NotificationAddressee addressee, Enums.Types.NotificationType type) throws IllegalArgumentException {
        if (addressee.equals(NotificationAddressee.RECIPIENT) && !Arrays.asList(NotificationType.Delivered, NotificationType.Sent).contains(type))
            throw new IllegalArgumentException("Sending this type of notification to order recipient is not allowed: " + type.toString());
        Client messageAddressee = null;
        switch (addressee) {
            case SENDER -> messageAddressee = this.sender;
            case RECIPIENT -> messageAddressee = this.recipient;
        }
        String message = messageAddressee.getFullName().toUpperCase(Locale.ROOT) + " hello, Order #" + order.getOrderId() + type.getValue();
        if (addressee.equals(NotificationAddressee.SENDER) && type.equals(NotificationType.Sent))
            message = message.concat(" to " +  recipient.getFullName());
        if (type.equals(NotificationType.Sent)) {
            LocalDateTime deliveryDate = LocalDateTime.now().plusSeconds(order.getDeliveryTime()/1000 + 1);
            message = message.concat(". Estimated delivery: " + Parsers.dateTimeConverter(deliveryDate, true) + ".");
        }
        return message;
    }

    /**
     * Sends the notification via configured channel (SMS, Email)
     * @param message test message that needs to be sent
     */
    private void sendNotification(String message) {
        ServerManager.sendToClient(message); //send notification via SMS API
        var param = new Pair<>(NotificationFields.Delivered.getValue(), "1");
        DBConnect.updateById(RequestType.NOTIFICATIONS, getNotificationId(), new ArrayList<>(Arrays.asList(param)));
    }

    public String[] sqlInsertValues() {
        return Arrays.asList(
                getNotificationRecipient(), getOrderId(), getSentDateTime().toString(), getMessage()
        ).toArray(new String[0]);
    }
}
