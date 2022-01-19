package Enums.Fields;

import java.util.Arrays;

public enum OrderFields {
    OrderId("orderId"),
    SenderId("senderId"),
    RecipientId("recipientId"),
    OrderDateTime("orderDateTime"),
    OrderStatus("orderStatus"),
    AssignedDroneId("assignedDroneId"),
    WithReturn("withReturn"),
    IsDeleted("isDeleted");

    private String name;
    OrderFields(String value) { this.name = value; }

    public static OrderFields[] sqlInsertColumns(){
        return Arrays.copyOfRange(values(),1, OrderFields.values().length-1);
    }
    public String getValue(){
        return this.name;
    }
}
