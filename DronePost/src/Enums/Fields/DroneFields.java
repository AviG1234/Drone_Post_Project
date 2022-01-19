package Enums.Fields;

import java.util.Arrays;

public enum DroneFields {
    DroneId("droneId"),
    DroneModel("droneModel"),
    DroneStatus("droneStatus"),
    IsDeleted("isDeleted");

    private String name;
    DroneFields(String name) { this.name = name; }

    public static DroneFields[] sqlInsertColumns(){
        return Arrays.copyOfRange(values(),0, DroneFields.values().length-1);
    }
    public String getValue(){
        return this.name;
    }


}

