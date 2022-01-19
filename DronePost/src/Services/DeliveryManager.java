package Services;

import Enums.Status.DroneStatus;
import Enums.Status.OrderStatus;
import OrderManagement.*;
import java.util.*;

public class DeliveryManager extends Thread {
    private final Location base = new Location(0, 0);
    private final Queue<Order> orders;
    private Boolean active = false;
    private Order currentOrder;
    private Drone deliveryDrone;
    private ArrayList<Drone> drones;

    public DeliveryManager(ArrayList<Drone> drones) {
        restoreInTransitOrders();
        ArrayList<Order> paidOrders = new ArrayList<>();
        try {
            DBConnect.getPaidOrders().forEach(orderDetails -> paidOrders.add(new Order(orderDetails)) );
        }
        catch (ClassNotFoundException e) { e.printStackTrace(); }
        this.orders = new LinkedList<>();
        orders.addAll(paidOrders);
        if (drones.isEmpty())
            throw new NullPointerException("Drones' list is empty.");
        else this.drones = drones;

        startDelivery();
    }

    public void addOrder(Order order) {
        this.orders.add(order);
        startDelivery();
    }

    private Drone getSuitableDrone(Order order) {
        Location senderLocation = order.getSender().getLocation();
        Location recipientLocation = order.getRecipient().getLocation();
        int distance = senderLocation.distanceTo(recipientLocation) + recipientLocation.distanceTo(base);
        order.setDeliveryTime(distance);
        for (Drone drone : drones) {
            if (drone.getDroneStatus().equals(DroneStatus.Available.getValue())) {
                int totalDistanceInMilliseconds = distance + drone.getDroneLocation().distanceTo(senderLocation);
                if (totalDistanceInMilliseconds < drone.getBatteryLifeInMilliseconds()) {
                    drone.setDroneStatus(DroneStatus.InTransit, true);
                    return drone;
                }
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getSuitableDrone(order);
    }

    private Drone assignDroneToTask(Order order) {
        Drone drone = getSuitableDrone(order);
        if (!drone.getDroneId().isBlank()) {
            order.setDrone(drone);
            orders.remove();
            return drone;
        }
        return null;
    }

    public void startDelivery() {
        active = true;
        try {
            new Thread( () -> {
                while (active) {
                    if (!orders.isEmpty())
                        new Thread( () -> {
                            deliveryDrone = null;
                            currentOrder = orders.peek();
                            while (deliveryDrone == null){
                                deliveryDrone = assignDroneToTask(currentOrder);
                            }
                            deliveryDrone.deliverOrder(currentOrder);
                        }).run();
                    else active = false;
                }
            }).run();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    private void restoreInTransitOrders(){
        ArrayList<Order> inTransitOrders = new ArrayList<>();
        try {
            DBConnect.getInTransitOrders().forEach(orderDetails -> inTransitOrders.add(new Order(orderDetails)) );
            inTransitOrders.forEach( order -> order.setOrderStatus(OrderStatus.Paid, true));
        }
        catch (ClassNotFoundException e) { e.printStackTrace(); }
    }
}
