package UserManagement;

import Enums.Fields.ClientFields;
import Enums.Types.RequestType;
import Services.DBConnect;
import Enums.Status.OrderStatus;
import OrderManagement.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import OrderManagement.Location;
import org.javatuples.Pair;

public class Client extends Person {
    private String clientId;
    private int posX;
    private int posY;
    private String subscriptionCode;
    private int numberOfDeliveriesLeft;
    private String isDeleted = "0";
    private final ArrayList<String> subscriberCodes = new ArrayList<>(Arrays.asList("1", "2"));
    private Location location;


    /**
     * Create the instance of Users.Client, that extends Users.Person.
     * @param clientDetails - ordered list of data: clientId, personalId, firstName, lastName, dateOfBirth,
     *                        street, building, city, zipCode, email, telephone, subscriptionCode.
     *                        In case the client is being restored from the DB, additional data shall appear:
     *                        posX, posY, numberOfDeliveriesLeft, isDeleted
     * @param fromUI - If true the constructor will set posX & posY randomly, numberOfDeliveriesLeft (based on the subscriptionCode),
     *                 isDeleted (default value >> 0)
     */
    public Client(ArrayList<String> clientDetails, Boolean fromUI){
        super(new ArrayList<>(clientDetails.subList(1,11)));
        if (fromUI) {
            this.clientId = clientDetails.get(0);
            updateSubscription(clientDetails.get(11));
            this.location = new Location();
            this.posX = this.location.getPosX();
            this.posY = this.location.getPosY();
        } else {
            this.clientId = clientDetails.get(0);
            this.subscriptionCode = clientDetails.get(11);
            this.posX = Integer.parseInt(clientDetails.get(12));
            this.posY = Integer.parseInt(clientDetails.get(13));
            this.location = new Location(this.posX, this.posY);
            setNumberOfDeliveriesLeft(Integer.parseInt(clientDetails.get(14)), false);
            this.isDeleted = clientDetails.get(15);
        }
    }

    /**
     * Create order and assign a drone to it
     * @param recipient of type Client
     * @return object of type Order
     */
    private Order createOrder(Client recipient, Boolean withReturn){
        try{
            Order order = new Order(this, recipient, LocalDateTime.now().withNano(0), OrderStatus.Paid, withReturn);
            String orderId = DBConnect.insertOrder(order);
            if (Integer.parseInt(orderId) < 1)
                throw new SQLException("Unable to save the order to database.");
            order.setOrderId(orderId);
            this.setNumberOfDeliveriesLeft(this.getNumberOfDeliveriesLeft()-1, true);
            return order;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    //This method will always create orders without return
    public Order createOrder(Client recipient) {
        return createOrder(recipient, false);
    }

    public String getClientId() {
        return clientId;
    }
    public String getSubscriptionCode() {
        return subscriptionCode;
    }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getNumberOfDeliveriesLeft() {
        return numberOfDeliveriesLeft;
    }
    public Location getLocation(){ return this.location;}

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setNumberOfDeliveriesLeft(int numberOfDeliveriesLeft, Boolean updateDB) throws IllegalArgumentException {
        int max = 0;
        if (!subscriberCodes.contains(subscriptionCode))
            throw new IllegalArgumentException("Invalid subscription code");
        switch (this.subscriptionCode){
            case "1" -> max = 50;
            case "2" -> max = 150;
        }
        if (numberOfDeliveriesLeft < 0 || numberOfDeliveriesLeft > max)
            throw new IllegalArgumentException("Invalid number of deliveries left.");
        this.numberOfDeliveriesLeft = numberOfDeliveriesLeft;
        if (updateDB) {
            var param = new Pair<>(ClientFields.NumberOfDeliveriesLeft.getValue(), String.valueOf(numberOfDeliveriesLeft));
            DBConnect.updateById(RequestType.CLIENTS, this.getClientId(), new ArrayList<>(Arrays.asList(param)));
        }
    }
    public void updateSubscription(String subscriptionCode) throws IllegalArgumentException {
        this.numberOfDeliveriesLeft = 0;
            if (!subscriberCodes.contains(subscriptionCode))
                throw new IllegalArgumentException("Invalid subscription code");
            this.subscriptionCode = subscriptionCode;
            switch (this.subscriptionCode){
                case "1" -> numberOfDeliveriesLeft += 50;
                case "2" -> numberOfDeliveriesLeft += 150;
            }
    }

    public String[] sqlInsertValues(){
        return Arrays.asList(
                getPersonalId(), getFirstName(), getLastName(), getDateOfBirth().toString(),
                getStreet(), getBuilding(), getCity(), getZipCode(), getEmail(), getTelephone(),
                String.valueOf(getSubscriptionCode()), String.valueOf(getPosX()),
                String.valueOf(getPosY()), String.valueOf(getNumberOfDeliveriesLeft())
        ).toArray(new String[0]);
    }
}
