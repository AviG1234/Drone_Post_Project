package OrderManagement;

import Enums.Fields.OrderFields;
import Services.Parsers;
import Services.DBConnect;
import Enums.Status.OrderStatus;
import Enums.Types.RequestType;
import UserManagement.Client;
import org.javatuples.Pair;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;

public class Order {
    private String orderId;
    private Client sender;
    private Client recipient;
    private LocalDateTime orderDateTime;
    private OrderStatus orderStatus;
    private int deliveryTime;
    private String assignedDroneId = "-1";
    private Boolean withReturn;
    private String isDeleted;
    private Drone drone = null;

    public Order(Client sender, Client recipient, LocalDateTime orderDateTime, OrderStatus orderStatus, Boolean withReturn) {
        try {
            setSenderRecipient(sender, recipient);
            setOrderDateTime(orderDateTime);
            setOrderStatus(orderStatus,false);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        this.withReturn = withReturn;
        this.isDeleted = "0";
    }

    public Order (ArrayList<String> order){
        try{
            setOrderId(order.get(0));
            Client sender = new Client(DBConnect.getById(RequestType.CLIENTS, order.get(1)), false);
            Client recipient = new Client(DBConnect.getById(RequestType.CLIENTS, order.get(2)), false);
            setSenderRecipient(sender, recipient);
            setOrderDateTime(order.get(3));
            setOrderStatus(OrderStatus.getStatus(order.get(4)), false);
            setAssignedDroneId(order.get(5), false);
            setWithReturn(order.get(6));
            setIsDeleted(order.get(7));
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    public String getOrderId(){ return orderId; }
    public String getSenderId() { return sender.getClientId(); }
    public String getRecipientId() { return recipient.getClientId(); }
    public Drone getDrone(){
        return this.drone;
    }
    public Client getSender(){ return this.sender;}
    public Client getRecipient(){ return this.recipient;}
    public LocalDateTime getOrderDateTime() { return orderDateTime; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public int getDeliveryTime() { return  deliveryTime; }
    public String getAssignedDroneId() {
        return assignedDroneId;
    }
    public Boolean getWithReturn() { return withReturn; }

    public void setDrone(Drone drone){
        this.drone = drone;
        try {
            setAssignedDroneId(this.drone.getDroneId(), true);
        } catch (IllegalArgumentException iae) {System.out.println(iae.getMessage());}
    }
    public void setOrderId(String orderId){
        this.orderId = orderId;
    }
    public void setSenderRecipient(Client sender, Client recipient) throws IllegalArgumentException {
        if (sender.getClientId().equals("-1")
                || recipient.getClientId().equals("-1")
                || sender.getClientId().equals(recipient.getClientId())
            )
            throw new IllegalArgumentException("Incorrect sender and/or recipient provided.");
        else { this.sender = sender; this.recipient = recipient; }
    }
    public void setOrderDateTime (LocalDateTime orderDateTime) throws IllegalArgumentException{
        if (orderDateTime.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Incorrect order Date/Time provided.");
        this.orderDateTime = orderDateTime.truncatedTo(ChronoUnit.SECONDS);
    }
    public void setOrderDateTime (String orderDateTime) {
        setOrderDateTime(Parsers.dateTimeConverter(orderDateTime));
    }
    public void setOrderStatus(OrderStatus orderStatus, Boolean updateDB) throws IllegalArgumentException {
        if (orderStatus == null)
            throw new IllegalArgumentException("Non-existing order status provided.");
        else {
            this.orderStatus = orderStatus;
            if (updateDB) {
                var param = new Pair<>(OrderFields.OrderStatus.getValue(), getOrderStatus().getValue());
                DBConnect.updateById(RequestType.ORDERS, this.getOrderId(), new ArrayList<>(Arrays.asList(param)));
            }
        }
    }
    public void setDeliveryTime(int deliveryTime) throws IllegalArgumentException {
        if (deliveryTime > 0)
            this.deliveryTime = deliveryTime;
        else throw new IllegalArgumentException("Incorrect delivery time.");
    }
    public void setAssignedDroneId(String assignedDroneId, Boolean updateDB) throws IllegalArgumentException {
        int id = Integer.parseInt(assignedDroneId);
        if (id == -1 || (id > 0 && id < 21)) {
            this.assignedDroneId = assignedDroneId;
            if (updateDB) {
                try {
                    var param = new Pair<>(OrderFields.AssignedDroneId.getValue(), assignedDroneId);
                    Boolean flag = DBConnect.updateById(RequestType.ORDERS, this.getOrderId(), new ArrayList<>(Arrays.asList(param)));
                    if (!flag)
                        throw new SQLException("Unable to update drone in database.");
                } catch (SQLException e) {e.printStackTrace();}
            }
        }
        else throw new IllegalArgumentException("Incorrect drone ID.");
    }
    public void setWithReturn(String withReturn) throws IllegalArgumentException {
        if (withReturn.equals("0") || withReturn.equals("1"))
            this.withReturn = withReturn.equals("1");
        else
            throw new IllegalArgumentException("Wrong withReturn value.");
    }
    public void setIsDeleted(String isDeleted) throws IllegalArgumentException {
        if (!Arrays.asList("0","1").contains(isDeleted))
            throw new IllegalArgumentException("Incorrect isDeleted status.");
        this.isDeleted = isDeleted;
    }

    public String[] sqlInsertValues(){
        return Arrays.asList(
                getSenderId(), getRecipientId(), getOrderDateTime().toString(),
                getOrderStatus().getValue(), getAssignedDroneId(), getWithReturn() ? "1" : "0"
        ).toArray(new String[0]);
    }
}
