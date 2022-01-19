package Enums.Fields;

import java.util.Arrays;

public enum MessageFields {
    MessageId("messageId"),
    SenderId("senderId"),
    RecipientId("recipientId"),
    SentDateTime("sentDateTime"),
    Message("message"),
    Delivered("delivered");

    private String name;
    MessageFields(String name) { this.name = name; }


    public static MessageFields[] sqlInsertColumns(){
        return Arrays.copyOfRange(values(),1, MessageFields.values().length-1);
    }
    public String getValue(){
        return this.name;
    }
}

