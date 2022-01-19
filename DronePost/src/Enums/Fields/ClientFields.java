package Enums.Fields;

import java.util.ArrayList;
import java.util.Arrays;

public enum ClientFields {
    ClientId("clientId"),
    PersonalId("personalId"),
    FirstName("firstName"),
    LastName("lastName"),
    DateOfBirth("dateOfBirth"),
    Street("street"),
    Building("building"),
    City("city"),
    ZipCode("zipCode"),
    Email("email"),
    Telephone("telephone"),
    SubscriptionCode("subscriptionCode"),
    PosX("posX"),
    PosY("posY"),
    NumberOfDeliveriesLeft("numberOfDeliveriesLeft"),
    IsDeleted("isDeleted");

    private String name;
    ClientFields(String name) { this.name = name; }

    public static ArrayList<String> personColumns(){
        ArrayList<String> columns = new ArrayList<>();
        ClientFields[] person = Arrays.copyOfRange(values(),1, ClientFields.values().length-2);
        for (ClientFields cf : person){
            columns.add(cf.getValue());
        }
        return columns;//Arrays.asList(person.).forEach((item) -> item.getValue());
    }
    public static ClientFields[] sqlInsertColumns(){
        return Arrays.copyOfRange(values(),1, ClientFields.values().length-1);
    }
    public String getValue(){
        return this.name;
    }
}

