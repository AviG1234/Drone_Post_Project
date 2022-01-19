package UserManagement;

import Enums.CommunicationCode;
import Enums.Fields.MessageFields;
import Enums.Types.RequestType;
import Services.DBConnect;
import Services.Parsers;
import Services.ServerManager;
import org.javatuples.Pair;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;

public class Messages {
    private String messageId;
    private String senderId;
    private String recipientId;
    private LocalDateTime sentDateTime;
    private String message;
    private Boolean delivered;
    private ArrayList<String> messageData;
    private ArrayList<String> clientResponse;

    public Messages(ArrayList<String> messageData) {
        try {
            this.messageData = messageData;
            setSender(messageData.get(0));
            setRecipient(messageData.get(1));
            setMessage(messageData.get(2));
            setSentDateTime(LocalDateTime.now());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getMessageId() {
        return this.messageId;
    }

    public String getSenderId() {
        return this.senderId;
    }

    public String getRecipientId() {
        return this.recipientId;
    }

    public LocalDateTime getSentDateTime() {
        return this.sentDateTime;
    }

    public String getMessage() {
        return this.message;
    }

    public String getDelivered() { return this.delivered ? "1" : "0"; }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setSender(String senderTelephone) throws ClassNotFoundException {
        this.senderId = DBConnect.getClientIdByTelephone(senderTelephone);
    }

    public void setRecipient(String recipientTelephone) throws ClassNotFoundException {
        this.recipientId = DBConnect.getClientIdByTelephone(recipientTelephone);
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

    public void updateMessageDelivered(Boolean updateDB) {
        this.delivered = true;
        ServerManager.sendToClient(Parsers.parseMessageToClient(CommunicationCode.MessageDelivered, this.clientResponse));
        if (updateDB) {
            var param = new Pair<>(MessageFields.Delivered.getValue(), getDelivered() );
            DBConnect.updateById(RequestType.MESSAGES, this.getMessageId(), new ArrayList<>(Arrays.asList(param)));
        }
    }

    /**
     * Sends the message to recipient
     */
    public void sendMessage() {
        String messageReceiver = Parsers.parseMessageToClient(CommunicationCode.NewMessage, this.messageData);
        ServerManager.sendToClient(messageReceiver); //send notification via SMS API
    }

    public ArrayList<String> awaitClientResponse() {
        String messageFromClient = ServerManager.receiveFromClient();
        Pair<CommunicationCode, ArrayList<String>> parsedMessage;
        if (messageFromClient != null) {
            parsedMessage = Parsers.parseMessageFromClient(messageFromClient);
            System.out.println(parsedMessage);
            if (parsedMessage.getValue0().equals(CommunicationCode.MessageReceived)
                && getSenderId().equals(parsedMessage.getValue1().get(1))
                && getRecipientId().equals(parsedMessage.getValue1().get(0))
                && getMessage().equals(parsedMessage.getValue1().get(2)))
                this.clientResponse = parsedMessage.getValue1();
        }
        return clientResponse;
    }

    public String[] sqlInsertValues() {
        return Arrays.asList(
                getSenderId(), getRecipientId(), getSentDateTime().toString(), getMessage()
        ).toArray(new String[0]);
    }
}
