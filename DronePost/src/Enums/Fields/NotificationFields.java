package Enums.Fields;

import java.util.Arrays;

public enum NotificationFields {
    NotificationId("notificationId"),
    RecipientId("recipientId"),
    OrderId("orderId"),
    SentDateTime("sentDateTime"),
    Message("message"),
    Delivered("delivered");

    private String name;
    NotificationFields(String name) { this.name = name; }


    public static NotificationFields[] sqlInsertColumns(){
        return Arrays.copyOfRange(values(),1, NotificationFields.values().length-1);
    }
    public String getValue(){
        return this.name;
    }
}

