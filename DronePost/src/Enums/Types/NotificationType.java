package Enums.Types;

public enum NotificationType {
    Delivered(" has been delivered."),
    Sent(" is on the way");

    private String name;
    NotificationType(String name){ this.name = name; }

    public String getValue(){
        return this.name;
    }

    public static NotificationType getNotification(String name){
        for(NotificationType value : values()){
            if (value.getValue().equals(name))
                return value;
        }
        return null;
    }
}
