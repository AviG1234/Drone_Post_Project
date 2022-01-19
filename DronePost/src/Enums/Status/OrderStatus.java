package Enums.Status;

public enum OrderStatus {
    Paid("paid"),
    InTransit("inTransit"),
    Delivered("delivered");

    private final String name;
    OrderStatus(String name) { this.name = name; }

    public String getValue(){
        return this.name;
    }
    public static OrderStatus getStatus(String name){
        for(OrderStatus value : values()){
            if (value.getValue().equals(name))
                return value;
        }
        return null;
    }
}