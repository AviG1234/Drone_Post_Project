package Services;

import Enums.CommunicationCode;
import org.javatuples.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;

public class Parsers {

    public static LocalDateTime dateTimeConverter(String dateTime) {
        if (dateTime.contains("."))
            dateTime = dateTime.split("\\.")[0];
        if (dateTime.contains(" "))
            dateTime = dateTime.replace(" ","T");
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    public static String dateTimeConverter(LocalDateTime dateTime, Boolean forNotification) {
        if (!forNotification)
            return dateTime.truncatedTo(ChronoUnit.SECONDS).toString().replace("T", " ");
        return dateTime.truncatedTo(ChronoUnit.MINUTES).toString().replace("T", " at ");
    }
    public static ArrayList<ArrayList<String>> parseSentReceivedOrders (ArrayList<ArrayList<String>> orders, String clientId){
        orders.forEach(order ->{
            if (order.get(1).equals(clientId))
                order.add("Sender");
            if (order.get(2).equals(clientId))
                order.add("Recipient");
        });
        return orders;
    }
    public static Pair<CommunicationCode, ArrayList<String>> parseMessageFromClient (String message) {
        CommunicationCode code = null;
        ArrayList<String> messageContent = new ArrayList<>();
        try {
            if (!message.contains(";")) {
                throw new IllegalArgumentException("Received invalid message from the client.");
            }
            String[] messageSplit = message.split(";");
            code = parseCode(messageSplit[0]);
            messageContent = parseMessage(messageSplit);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            ServerManager.sendToClient(Parsers.parseMessageToClient(CommunicationCode.ServerProcessingError, null));
        }
        return new Pair<>(code, messageContent);
    }
    public static String parseMessageToClient (CommunicationCode code, ArrayList<String> data){
        StringBuilder response = new StringBuilder().append(code.getCodeId()).append(";");
        if (data != null)
            response.append(String.join(";", data));
        return response.toString();
    }

    private static CommunicationCode parseCode(String code){
        CommunicationCode cc;
        if (!CommunicationCode.codes().contains(code))
            throw new IllegalArgumentException("Invalid message code");
        else cc = CommunicationCode.getCode(code);
        return cc;
    }
    private static ArrayList<String> parseMessage (String[] messageSplit) throws IllegalArgumentException {
        ArrayList<String> messageContent = new ArrayList<>();
        for (int i = 1; i < messageSplit.length; ++i) {
            if (messageSplit[i].isBlank())
                throw new IllegalArgumentException("Invalid message");
            else messageContent.add(messageSplit[i]);
        }
        return messageContent;
    }
}
