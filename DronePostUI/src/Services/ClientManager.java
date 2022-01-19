package Services;

import DialogsUI.LoginForm;
import DialogsUI.MainForm;
import DialogsUI.ReceivedMessageForm;
import DialogsUI.RegistrationForm;
import Enums.CommunicationCode;
import org.javatuples.Pair;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientManager {
    private static final String appName = "Drone Post";
    private static Socket clientSocket;
    private static String host = "localhost";
    private static int port = 9000;
    private static BufferedReader fromServer;
    private static PrintStream toServer;
    private static String currentUserId;

    public static void startClient() {
        try {
            clientSocket = new Socket(host, port);
            System.out.println("Connected to server..." + clientSocket.getInetAddress() + " " + clientSocket.getPort());
            fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            toServer = new PrintStream(clientSocket.getOutputStream(), true);

            openLoginForm();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String receiveFromServer() {
        try {
            return fromServer.readLine();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        return null;
    }

    public static void sendToServer(String message) {
        toServer.println(message);
    }

    public static void setCurrentUserId(String loggedInUserId) {
        if (loggedInUserId != null && !loggedInUserId.isBlank())
            currentUserId = loggedInUserId;
    }

    public static void openLoginForm() {
        new LoginForm(appName);
    }

    public static void openRegistrationForm(JFrame form) {
        form.dispose();
        new RegistrationForm(appName);
    }

    public static void openOrderForm(JFrame form, ArrayList<String> currentUserData) {
        form.dispose();
        new MainForm(appName, currentUserData);
    }

    public static Boolean validateTelephone(String telephone) {
        if (telephone.length() != 13) return false;
        String message = Parsers.parseMessageToServer(CommunicationCode.ValidateTelephone, new ArrayList<>(Arrays.asList(telephone)));
        ClientManager.sendToServer(message);
        var response = Parsers.parseMessageFromServer(ClientManager.receiveFromServer());
        if (response.getValue0() == CommunicationCode.ValidateTelephone)
            switch (response.getValue1().get(0)) {
                case "valid":
                    return true;
                case "invalid":
                    return false;
                default:
                    System.out.println("Received unknown response validating the telephone.");
            }
        return false;
    }

    public static void stop() {
        try {
            fromServer.close();
            toServer.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processServerResponse() {
        new Thread(() -> {
            while (!clientSocket.isClosed()) {
                Pair<CommunicationCode, ArrayList<String>> parsedMessage = Parsers.parseMessageFromServer(receiveFromServer());
                switch (parsedMessage.getValue0()) {
                    //case ReceiverNotConnectedToServer -> new ReceivedMessageForm("server", "Client not connected to server");;
                    case NewMessage -> {
                        if (parsedMessage.getValue1().get(0).equals(currentUserId)) {
                            new Thread(() -> {
                                new ReceivedMessageForm(parsedMessage.getValue1().get(1), parsedMessage.getValue1().get(2));
                                ArrayList<String> data = new ArrayList<>(Arrays.asList(parsedMessage.getValue1().get(1), parsedMessage.getValue1().get(0), parsedMessage.getValue1().get(2)));
                                sendToServer(Parsers.parseMessageToServer(CommunicationCode.MessageReceived, data));
                            }).run();
                        }
                    }
                    case MessageDelivered -> {//receive message.
                        new ReceivedMessageForm(parsedMessage.getValue1().get(1), "Message delivered");
                        return;
                    }
                    case OrderSent, OrderDelivered -> {
                        if (parsedMessage.getValue1().get(0).equals(currentUserId))
                            new ReceivedMessageForm("Drone", parsedMessage.getValue1().get(1));
                    }
                    case GetClientDetails -> {
                        setCurrentUserId(parsedMessage.getValue1().remove(0));
                        new Services().saveClientDetailsToFile(parsedMessage.getValue1());
                        return;
                    }
                    case ServerProcessingError -> throw new NullPointerException("Server processing error due to NULL message.");
                    case HistoryReport -> {
                        if (parsedMessage.getValue1().isEmpty())
                            new ReceivedMessageForm("Server", "You have not sent/received any orders yet.");
                        else new ReceivedMessageForm("Server", parsedMessage.getValue1());
                        return;
                    }
                }
            }
        }).start();
    }
}
