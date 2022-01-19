package OrderManagement;

import Enums.Fields.DroneFields;
import Enums.NotificationAddressee;
import Enums.Status.OrderStatus;
import Enums.Types.NotificationType;
import Services.DBConnect;
import Enums.Status.DroneStatus;
import Enums.Types.RequestType;
import Services.LogManager;
import org.javatuples.Pair;

import java.util.*;

public class Drone {
    private String droneId;
    private String droneModel;
    private String droneStatus;
    private Order currentOrder;
    private int batteryLifeInMilliseconds;
    private String isDeleted = "0";
    final int defaultBatteryLife = 3 * 60 * 60;
    private final ArrayList<String> droneModels = new ArrayList<>(Arrays.asList("ModelA", "ModelB", "ModelC", "ModelD"));
    private Location base;
    private Location location;
    private int refreshRate = 100;
    private Timer timer;

    public Drone() {
    }

    public Drone(String droneId, DroneStatus status) {
        setDroneId(droneId);
        setModel(null);
        this.droneStatus = status.getValue();
        this.location = this.base = new Location(0, 0);
        this.batteryLifeInMilliseconds = 1000 * defaultBatteryLife;
    }

    public Drone(ArrayList<String> droneDetails) {
        try {
            setDroneId(droneDetails.get(0));
            setModel(droneDetails.get(1));
            setDroneStatus(DroneStatus.getStatus(droneDetails.get(2)), false);
            this.location = this.base = new Location(0, 0);
            this.batteryLifeInMilliseconds = 1000 * defaultBatteryLife;
            setIsDeleted(droneDetails.get(3));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public String getDroneId() {
        return droneId;
    }

    public String getDroneModel() {
        return droneModel;
    }

    public String getDroneStatus() {
        return droneStatus;
    }

    public Location getDroneLocation() {
        return location;
    }

    public int getBatteryLifeInMilliseconds() {
        return batteryLifeInMilliseconds;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public void setModel(String droneModel) throws IllegalArgumentException {
        if (droneModel == null || droneModel.isEmpty())
            this.droneModel = droneModels.get(new Random().nextInt(4));
        else if (!droneModels.contains(droneModel))
            throw new IllegalArgumentException("Unknown drone model");
        else this.droneModel = droneModel;
    }

    public void setDroneStatus(DroneStatus droneStatus, Boolean updateDB) throws IllegalArgumentException {
        if (droneStatus == null)
            throw new IllegalArgumentException("Non-existing drone status provided.");
        else {
            this.droneStatus = droneStatus.getValue();
            if (updateDB) {
                var param = new Pair<>(DroneFields.DroneStatus.getValue(), this.droneStatus);
                DBConnect.updateById(RequestType.DRONES, this.getDroneId(), new ArrayList<>(Arrays.asList(param)));
            }
        }
    }

    private void setIsDeleted(String isDeleted) throws IllegalArgumentException {
        if (!Arrays.asList("0", "1").contains(isDeleted))
            throw new IllegalArgumentException("Incorrect isDeleted status.");
        this.isDeleted = isDeleted;
    }

    public String[] sqlInsertValues() {
        return Arrays.asList(
                getDroneId(), getDroneModel(), getDroneStatus()).toArray(new String[0]);
    }

    public ArrayList<Drone> getAllDrones() {
        ArrayList<ArrayList<String>> dronesData = DBConnect.getAll(RequestType.DRONES);
        ArrayList<Drone> drones = new ArrayList<>();
        int numberOfDrones = dronesData.size();
        if (numberOfDrones >= 20)
            dronesData.forEach(data -> drones.add(new Drone(data)));
        int retryCounter = 0;
        while (numberOfDrones < 21 && retryCounter < 3) {
            Drone drone = new Drone(String.valueOf(++numberOfDrones), DroneStatus.Available);
            String insertResult = DBConnect.insertDrone(drone);
            switch (insertResult) {
                case "1" -> drones.add(drone);
                case "0" -> {
                    System.out.println("Writing to database error.");
                    ++retryCounter;
                }
                case "-1" -> {
                    System.out.println("Connection to database error.");
                    ++retryCounter;
                }
            }
        }
        if (retryCounter == 3)
            System.out.println("Error creating drones.");
        return drones;
    }

    public void deliverOrder(Order order) {
        timer = new Timer();
        currentOrder = order;

        Location sender = order.getSender().getLocation();
        Location recipient = order.getRecipient().getLocation();

        String message0 = ": Drone (id " + currentOrder.getDrone().getDroneId() + ") is on it's way to pick up the order #"
                + currentOrder.getOrderId() + " from " + currentOrder.getSender().getFullName();
        String message1 = ": Drone (id " + currentOrder.getDrone().getDroneId() + ") has picked up the order #"
                + currentOrder.getOrderId() + " from " + currentOrder.getSender().getFullName();
        String message2 = ": Drone (id " + currentOrder.getDrone().getDroneId() + ") has delivered the order #"
                + currentOrder.getOrderId() + " to " + currentOrder.getRecipient().getFullName();
        String message3 = ": Drone (id " + currentOrder.getDrone().getDroneId() + ") has arrived at base and is fully charged";

        var onWayToSender = new Update(DroneStatus.InTransit, message0, currentOrder, OrderStatus.Paid);
        var packagePickedUp = new Update(DroneStatus.InTransit, message1, currentOrder, OrderStatus.InTransit);
        var packageDelivered = new Update(DroneStatus.Available, message2, currentOrder, OrderStatus.Delivered);
        var backAtBase = new Update(DroneStatus.Available, message3, currentOrder, null);

        var toBase = new Travel(recipient, this.base, backAtBase, null);
        var toRecipient = new Travel(sender, recipient, packageDelivered, toBase);
        var toSender = new Travel(this.location, sender, packagePickedUp, toRecipient);

        onWayToSender.execute();

        timer.schedule(toSender, 0, refreshRate);
    }

    class Update {
        private final DroneStatus updatedDroneStatus;
        private final String message;
        private final Order order;
        private final OrderStatus updatedOrderStatus;

        public Update(DroneStatus updatedDroneStatus, String message, Order order, OrderStatus updatedOrderStatus) {
            this.updatedDroneStatus = updatedDroneStatus;
            this.message = message;
            this.order = order;
            this.updatedOrderStatus = updatedOrderStatus;
        }

        public void execute() {
            if (!getDroneStatus().equals(this.updatedDroneStatus.getValue()))
                setDroneStatus(this.updatedDroneStatus, true);
            if (location.equals(base))
                batteryLifeInMilliseconds = 1000 * defaultBatteryLife;
            if (updatedOrderStatus != null && !order.getOrderStatus().equals(updatedOrderStatus)) {
                order.setOrderStatus(updatedOrderStatus, true);
                switch (updatedOrderStatus) {
                    case InTransit -> {
                        new Thread(() -> new Notifications(order).notify(NotificationAddressee.SENDER, NotificationType.Sent)).start();
                        new Thread(() -> new Notifications(order).notify(NotificationAddressee.RECIPIENT, NotificationType.Sent)).start();
                    }
                    case Delivered -> {
                        new Thread(() -> new Notifications(order).notify(NotificationAddressee.SENDER, NotificationType.Delivered)).start();
                        new Thread(() -> new Notifications(order).notify(NotificationAddressee.RECIPIENT, NotificationType.Delivered)).start();
                    }
                }
            }
            LogManager.printLog(new Date() + message + "  ... battery left = " + batteryLifeInMilliseconds);
        }
    }

    class Travel extends TimerTask {

        private final Location from;
        private final Location to;
        private final Travel nextTrip;

        private final int distanceInMilliseconds;
        private final Update updateAtLocation;
        private int remainingTimeInMilliseconds;

        public Travel(Location from, Location to, Update updateAtLocation, Travel nextTrip) {
            this.from = from;
            this.to = to;
            this.remainingTimeInMilliseconds = this.distanceInMilliseconds = from.distanceTo(to);
            this.updateAtLocation = updateAtLocation;
            this.nextTrip = nextTrip;
        }

        public void run() {
            if (remainingTimeInMilliseconds > 0) {
                location = from.positionOnRoute(to, distanceInMilliseconds - remainingTimeInMilliseconds);
                remainingTimeInMilliseconds -= refreshRate;
            } else if (nextTrip != null) {
                this.cancel();
                location = to;
                this.updateAtLocation.execute();
                timer.schedule(this.nextTrip, 0, refreshRate);
            } else {
                this.cancel();
                if (droneStatus.equals(DroneStatus.Available.getValue())) {
                    location = to;
                    this.updateAtLocation.execute();
                    timer.cancel();
                    timer.purge();
                }
            }
            batteryLifeInMilliseconds -= refreshRate;
        }
    }
}
