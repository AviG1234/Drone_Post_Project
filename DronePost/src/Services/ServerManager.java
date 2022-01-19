package Services;

import Enums.CommunicationCode;
import Enums.Types.RequestType;
import OrderManagement.Drone;
import OrderManagement.Order;
import UserManagement.Client;
import UserManagement.Messages;
import org.javatuples.Pair;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerManager {
    public static ServerSocket serverSocket = null;
    private static final int port = 9000;
    public static Socket clientSocket;
    private static BufferedReader fromClient;
    private static PrintStream toClient;
    private static DeliveryManager deliveryManager;
    private static ArrayList<Drone> drones = new ArrayList<>();
    private static String phoneNumber = "";

    public static void startServer() {
        try {
            drones = new Drone().getAllDrones();
            deliveryManager = new DeliveryManager(drones);
            serverSocket = new ServerSocket(port);//or 127.0.0.1
            System.out.println("Connected to server..." + serverSocket.getInetAddress() + " " + serverSocket.getLocalPort());
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void start() throws IOException {
        while (true) {
            clientSocket = serverSocket.accept();
            new Thread(() -> {
                while (!clientSocket.isClosed()) {
                    try {
                        fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        toClient = new PrintStream(clientSocket.getOutputStream(), true);
                        Pair<CommunicationCode, ArrayList<String>> parsedMessage = Parsers.parseMessageFromClient(fromClient.readLine());
                        System.out.println(parsedMessage);
                        switch (parsedMessage.getValue0()) {
                            case RegisterClient -> processRegisterRequest(parsedMessage.getValue1());
                            case GetClientDetails -> processGetClientDetailsRequest(parsedMessage.getValue1());
                            case NewOrder -> processNewOrderRequest(parsedMessage.getValue1());
                            case NewMessage -> processNewMessageRequest(parsedMessage.getValue1());
                            case ValidateTelephone -> processValidateTelephoneRequest(parsedMessage.getValue1());
                            case HistoryRequest -> processHistoryRequest(parsedMessage.getValue1());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }).run();
        }
    }

    private static void processRegisterRequest(ArrayList<String> requestData) {
        requestData.add(0, "-1");
        Client client = new Client(requestData, true);
        String dbResponse = DBConnect.insertClient(client);
        switch (dbResponse) {
            case "-2" -> sendToClient(Parsers.parseMessageToClient(CommunicationCode.ClientAlreadyExists, null));
            case "-1" -> sendToClient(Parsers.parseMessageToClient(CommunicationCode.ClientSavingError, null));
            default -> {
                client.setClientId(dbResponse);
                sendToClient(Parsers.parseMessageToClient(CommunicationCode.ClientRegistered, null));
                phoneNumber = requestData.get(10);
            }
        }
    }

    private static void processGetClientDetailsRequest(ArrayList<String> requestData) {
        try {
            String id = DBConnect.getClientIdByTelephone(requestData.get(0));
            ArrayList<String> allDetails = null;
            if (id != null && !id.isBlank())
                allDetails = DBConnect.getById(RequestType.CLIENTS, id);
            if (allDetails.size() > 12)
                ServerManager.sendToClient(Parsers.parseMessageToClient(CommunicationCode.GetClientDetails, new ArrayList<>(allDetails.subList(0, 12))));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void processNewOrderRequest(ArrayList<String> requestData) {
        new Thread( () -> {
            try {
                String senderId = DBConnect.getClientIdByTelephone(requestData.get(0));
                String recipientId = DBConnect.getClientIdByTelephone(requestData.get(1));

                Client sender = new Client(DBConnect.getById(RequestType.CLIENTS, senderId), false);
                Client recipient = new Client(DBConnect.getById(RequestType.CLIENTS, recipientId), false);
                Order newOrder = sender.createOrder(recipient);
                deliveryManager.addOrder(newOrder);
            } catch (ClassNotFoundException cnf) {
                cnf.printStackTrace();
            }
        }).start();
    }

    private static void processNewMessageRequest(ArrayList<String> requestData) {
        new Thread( () -> {
            Messages message = new Messages(requestData);
            message.setMessageId(DBConnect.insertMessage(message));
            message.sendMessage();
            message.awaitClientResponse();
            message.updateMessageDelivered(true);
        }).start();

    }

    private static void processValidateTelephoneRequest(ArrayList<String> requestData) {
        new Thread ( () -> {
            try {
                String response = DBConnect.getClientIdByTelephone(requestData.get(0));
                if (!response.isBlank())
                    sendToClient(Parsers.parseMessageToClient(CommunicationCode.ValidateTelephone, new ArrayList<>(Arrays.asList("valid"))));
                else
                    sendToClient(Parsers.parseMessageToClient(CommunicationCode.ValidateTelephone, new ArrayList<>(Arrays.asList("invalid"))));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void processHistoryRequest(ArrayList<String> phone) {
        new Thread(() -> {
            try {
                String clientPhone = phone.get(0);
                String clientID = DBConnect.getClientIdByTelephone(clientPhone);
                ArrayList<ArrayList<String>> orders = DBConnect.getAllOrdersByClientId(clientID);
                StringBuilder order = new StringBuilder();
                for (ArrayList<String> o : orders) {
                    order.append("order ID: ").append(o.get(0)).append(", ");
                    order.append("sender ID: ").append(o.get(1)).append(", ");
                    order.append("recipient ID: ").append(o.get(2)).append(", ");
                    order.append("time of order: ").append(o.get(3)).append(", ");
                    if (o.get(4).equals("delivered")) {
                        order.append("order delivered");
                    } else {
                        order.append("order not yet delivered");
                    }
                    order.append(";");
                }
                sendToClient(Parsers.parseMessageToClient(CommunicationCode.HistoryReport,
                        new ArrayList<>(Arrays.asList(order.toString()))));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void sendToClient(String message) {
        try {
            System.out.println(message);
            if (toClient != null)
                toClient.println(message);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static String receiveFromClient() {
        try {
            return fromClient.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static void stop() {
        try {
            fromClient.close();
            toClient.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPhoneNumber() {
        return phoneNumber;
    }
}