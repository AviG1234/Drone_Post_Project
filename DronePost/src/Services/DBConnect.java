package Services;

import Enums.Fields.*;
import Enums.Types.RequestType;
import Enums.Status.OrderStatus;
import OrderManagement.*;
import UserManagement.*;
import com.healthmarketscience.sqlbuilder.*;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import java.sql.*;
import java.util.ArrayList;

public class DBConnect {
    private static final String baseUrl = "jdbc:mysql://localhost:3306/DronePost";
    private static final String dbUser = "root";
    private static final String dbPassword = "";

    private static ResultSet getQueryResults(String query) throws SQLException {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<ArrayList<String>> getAllOrdersByClientId(String clientId) {
        return getAllById(RequestType.ORDERS, clientId, null);
    }

    public static ArrayList<ArrayList<String>> getAll(RequestType type) {
        return getAllById(type, null, null);
    }

    /**
     * DBConnect API that returns data on the object by its ID.
     * @param type defines the table of enquiry, optional values come from RequestType enum
     * @param id  string value that is the object's ID in the relevant table in the DB
     * @return ArrayList<String> with data of the object
     */
    public static ArrayList<String> getById(RequestType type, String id) {
        ArrayList<String> parsedResults = new ArrayList<>(0);
        Quartet<String, Integer, String, String> params = getQueryParams(type);
        SelectQuery selectQuery = new SelectQuery().addAllColumns()
                .addCustomFromTable(params.getValue0())
                .addCondition(Converter.toConditionObject(params.getValue2() + id).setDisableParens(true));
        if (!type.equals(RequestType.NOTIFICATIONS))
            selectQuery.addCondition(Converter.toConditionObject(params.getValue3() + " = 0").setDisableParens(true));
        try (ResultSet resultSet = getQueryResults(selectQuery.toString())) {
            if (resultSet == null)
                throw new NullPointerException("No results received from the DB.");
            int index = 1;
            resultSet.next();
            while (index <= params.getValue1())
                parsedResults.add(resultSet.getString(index++));
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
        return parsedResults;
    }

    public static Boolean updateById(RequestType type, String id, ArrayList<Pair<String, String>> parameters) {
        Quartet<String, Integer, String, String> params = getQueryParams(type);
        int updated = -1;
        UpdateQuery updateQuery = new UpdateQuery(params.getValue0());
        try {
            if (parameters.isEmpty())
                throw new IllegalArgumentException("Property and/or value parameters are invalid.");
            parameters.forEach( item -> updateQuery.addCustomSetClause(item.getValue0(), item.getValue1()));
            updateQuery.addCondition(Converter.toConditionObject(params.getValue2() + id).setDisableParens(true));
            if (!type.equals(RequestType.NOTIFICATIONS) && !type.equals(RequestType.MESSAGES))
                updateQuery.addCondition(Converter.toConditionObject(params.getValue3() + " = 0").setDisableParens(true));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
        try (Connection connection = getConnection()) {
            System.out.println(updateQuery);
            updated = connection.prepareStatement(updateQuery.toString()).executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return updated == 1;
    }

    public static Boolean deleteById(RequestType type, String id) throws ClassNotFoundException {
        Quartet<String, Integer, String, String> params = getQueryParams(type);
        UpdateQuery updateQuery = (new UpdateQuery(params.getValue0())).addCustomSetClause(params.getValue3(), 1)
                .addCondition(Converter.toConditionObject(params.getValue2() + id).setDisableParens(true));
        int updated = -1;
        try (Connection connection = getConnection()) {
            updated = connection.prepareStatement(updateQuery.toString()).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updated == 1;
    }

    public static String insertClient(Client client) {
        InsertQuery insertQuery = (new InsertQuery(RequestType.CLIENTS.getTableName()))
                .addCustomColumns(ClientFields.sqlInsertColumns(), client.sqlInsertValues());
        String insertedId = "-1";
        try (Connection connection = getConnection()) {
            if (getClientIdByTelephone(client.getTelephone()).isBlank()) {
                connection.prepareStatement(insertQuery.toString()).executeUpdate();
                insertedId = getClientIdByTelephone(client.getTelephone());
            } else insertedId = "-2";   //code for "Provided phone number already exists"
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return insertedId;
    }

    public static String insertOrder(Order order) {
        InsertQuery insertQuery = (new InsertQuery(RequestType.ORDERS.getTableName()))
                .addCustomColumns(OrderFields.sqlInsertColumns(), order.sqlInsertValues());
        String insertedId = "-1";
        try (Connection connection = getConnection()) {
            connection.prepareStatement(insertQuery.toString()).executeUpdate();
            insertedId = getOrderIdBySenderIdOrderDateTime(order.getSenderId(), order.getOrderDateTime().toString());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return insertedId;
    }

    public static String insertDrone(Drone drone) {
        InsertQuery insertQuery = (new InsertQuery(RequestType.DRONES.getTableName()))
                .addCustomColumns(DroneFields.sqlInsertColumns(), drone.sqlInsertValues());
        String insertedId = "-1";
        try (Connection connection = getConnection()) {
            insertedId = String.valueOf(connection.prepareStatement(insertQuery.toString()).executeUpdate());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return insertedId;
    }

    public static String insertNotification(Notifications notification) {
        InsertQuery insertQuery = (new InsertQuery(RequestType.NOTIFICATIONS.getTableName()))
                .addCustomColumns(NotificationFields.sqlInsertColumns(), notification.sqlInsertValues());
        String insertedId = "-1";
        try (Connection connection = getConnection()) {
            connection.prepareStatement(insertQuery.toString()).executeUpdate();
            insertedId = getNotificationByRecipientOrderId(notification.getRecipientId(), notification.getOrderId(), notification.getSentDateTime().toString());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return insertedId;
    }

    public static String insertMessage(Messages message) {
        InsertQuery insertQuery = (new InsertQuery(RequestType.MESSAGES.getTableName()))
                .addCustomColumns(MessageFields.sqlInsertColumns(), message.sqlInsertValues());
        String insertedId = "-1";
        try (Connection connection = getConnection()) {
            connection.prepareStatement(insertQuery.toString()).executeUpdate();
            insertedId = getMessageBySenderRecipientOrderId(message.getSenderId(), message.getRecipientId(), message.getSentDateTime().toString());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return insertedId;
    }

    public static String getClientIdByTelephone(String telephone) throws ClassNotFoundException {
        SelectQuery selectQuery = new SelectQuery().addCustomColumns(Converter.toCustomColumnSqlObject(ClientFields.ClientId.toString()))
                .addCustomFromTable(RequestType.CLIENTS.getTableName())
                .addCondition(Converter.toConditionObject("telephone = '" + telephone + "'").setDisableParens(true))
                .addCondition(Converter.toConditionObject(ClientFields.IsDeleted.getValue() + " = 0").setDisableParens(true));
        return getId(selectQuery);
    }

    public static ArrayList<ArrayList<String>> getPaidOrders() throws ClassNotFoundException {
        return getAllById(RequestType.ORDERS, null, OrderStatus.Paid);
    }
    public static ArrayList<ArrayList<String>> getInTransitOrders() throws ClassNotFoundException {
        return getAllById(RequestType.ORDERS, null, OrderStatus.InTransit);
    }
    private static String getOrderIdBySenderIdOrderDateTime(String senderId, String orderDateTime) throws ClassNotFoundException {
        SelectQuery selectQuery = new SelectQuery().addCustomColumns(Converter.toCustomColumnSqlObject(OrderFields.OrderId.toString()))
                .addCustomFromTable(RequestType.ORDERS.getTableName())
                .addCondition(Converter.toConditionObject("senderId = " + senderId).setDisableParens(true))
                .addCondition(Converter.toConditionObject("orderDateTime = '" + orderDateTime + "'").setDisableParens(true))
                .addCondition(Converter.toConditionObject(OrderFields.IsDeleted.getValue() + " = 0").setDisableParens(true));
        return getId(selectQuery);
    }

    private static String getNotificationByRecipientOrderId(String recipientId, String orderId, String sentDateTime) throws ClassNotFoundException {
        SelectQuery selectQuery = new SelectQuery()
                .addCustomColumns(Converter.toCustomColumnSqlObject(NotificationFields.NotificationId.toString()))
                .addCustomFromTable(RequestType.NOTIFICATIONS.getTableName())
                .addCondition(Converter.toConditionObject("recipientId = " + recipientId).setDisableParens(true))
                .addCondition(Converter.toConditionObject("orderId = " + orderId).setDisableParens(true))
                .addCondition(Converter.toConditionObject("sentDateTime = '" + sentDateTime + "'").setDisableParens(true));
        return getId(selectQuery);
    }

    public static String getMessageBySenderRecipientOrderId(String senderId, String recipientId, String sentDateTime) throws ClassNotFoundException {
        SelectQuery selectQuery = new SelectQuery()
                .addCustomColumns(Converter.toCustomColumnSqlObject(MessageFields.MessageId.getValue()))
                .addCustomFromTable(RequestType.MESSAGES.getTableName())
                .addCondition(Converter.toConditionObject("senderId = " + senderId).setDisableParens(true))
                .addCondition(Converter.toConditionObject("recipientId = " + recipientId).setDisableParens(true))
                .addCondition(Converter.toConditionObject("sentDateTime = '" + sentDateTime + "'").setDisableParens(true));
        return getId(selectQuery);
    }

    /**
     *
     * @param type of RequestType list
     * @param clientId of the client to be queried. If the value is null, it will be disregarded
     * @param orderStatus of order to be queried. If value is null, it will be disregarded
     * @return the list of items, each one of which represents the list of details as per Request type
     */
    private static ArrayList<ArrayList<String>> getAllById(RequestType type, String clientId, OrderStatus orderStatus) {
        ArrayList<ArrayList<String>> parsedResults = new ArrayList<>(0);
        Quartet<String, Integer, String, String> params = getQueryParams(type);
        String query;
        SelectQuery selectQuery = new SelectQuery().addAllColumns()
                .addCustomFromTable(params.getValue0());

        //Adding condition to query for ALL orders
        boolean getAllOrders = type.equals(RequestType.ORDERS) && clientId != null && orderStatus == null;
        if (getAllOrders) {
            selectQuery.addCondition(buildSenderRecipientCondition(clientId).setDisableParens(true));
        }

        //Adding condition to query for ANY UNPROCESSED orders
        boolean getUnprocessedOrders = type.equals(RequestType.ORDERS) && clientId == null && orderStatus != null;
        if (getUnprocessedOrders) {
            selectQuery.addCondition(Converter.toConditionObject("orderStatus = '" + orderStatus.getValue() + "'").setDisableParens(true));
        }

        //adding isDeleted condition for queries except Notifications
        if (!type.equals(RequestType.NOTIFICATIONS))
            selectQuery.addCondition(Converter.toConditionObject(params.getValue3() + " = 0").setDisableParens(true));

        //removing brackets for all queries except get ANY UNPROCESSED orders
        query = orderStatus != null ? selectQuery.toString() : selectQuery.toString().replace("'", "");

        try (ResultSet resultSet = getQueryResults(query)) {
            if (resultSet == null)
                throw new NullPointerException("No results received from the DB.");
            while (resultSet.next()) {
                int index = 1;
                ArrayList<String> row = new ArrayList<>(0);
                while (index <= params.getValue1())
                    row.add(resultSet.getString(index++));
                parsedResults.add(row);
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
        return getAllOrders ? Parsers.parseSentReceivedOrders(parsedResults, clientId) : parsedResults;
    }

    private static Connection getConnection() throws ClassNotFoundException {
        int i = 1;
        Connection connection = null;
        while (connection == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(baseUrl, dbUser, dbPassword);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (++i == 4) break;
        }
        return connection;
    }

    private static String getId(SelectQuery selectQuery) {
        String parsedResults = "";
        try (ResultSet resultSet = getQueryResults(selectQuery.toString())) {
            if (!resultSet.next())
                throw new NullPointerException("No results received from the DB.");
            else parsedResults = resultSet.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println(e.getLocalizedMessage());
            return "";
        }
        return parsedResults;
    }

    private static Quartet<String, Integer, String, String> getQueryParams(RequestType type) {
        String table = type.getTableName();
        int colCount = 0;
        String condition = "";
        String deleteColumn = "";
        switch (type) {
            case CLIENTS -> {
                colCount = ClientFields.values().length;
                condition = "clientId = ";
                deleteColumn = ClientFields.IsDeleted.getValue();
            }
            case ORDERS -> {
                colCount = OrderFields.values().length;
                condition = "orderId = ";
                deleteColumn = OrderFields.IsDeleted.getValue();
            }
            case DRONES -> {
                colCount = DroneFields.values().length;
                condition = "droneId = ";
                deleteColumn = DroneFields.IsDeleted.getValue();
            }
            case NOTIFICATIONS -> {
                colCount = NotificationFields.values().length;
                condition = "notificationId = ";
            }
            case MESSAGES -> {
                colCount = MessageFields.values().length;
                condition = "messageId = ";
            }
        }
        return new Quartet<>(table, colCount, condition, deleteColumn);
    }

    private static ComboCondition buildSenderRecipientCondition(String clientId) {
        return ComboCondition.or(
                BinaryCondition.equalTo(OrderFields.SenderId.getValue(), clientId).setDisableParens(true),
                BinaryCondition.equalTo(OrderFields.RecipientId.getValue(), clientId).setDisableParens(true));
    }


}