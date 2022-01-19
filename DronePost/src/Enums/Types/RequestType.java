package Enums.Types;

public enum RequestType {
    CLIENTS("dronepost.clients"),
    ORDERS("dronepost.orders"),
    DRONES("dronepost.drones"),
    NOTIFICATIONS("dronepost.notifications"),
    MESSAGES("dronepost.messages");

    private String tableName;
    RequestType(String tableName) {this.tableName = tableName; }

    public String getTableName() {
        return tableName;
    }
}
