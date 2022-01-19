package Enums.Status;

public enum DroneStatus {
    Available("available"),
    InTransit("inTransit");

    private final String name;
    DroneStatus(String name) { this.name = name; }

    public String getValue(){
        return this.name;
    }
    public static DroneStatus getStatus(String name){
        for(DroneStatus value : values()){
            if (value.getValue().equals(name))
                return value;
        }
        return null;
    }
}

