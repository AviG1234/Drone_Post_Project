package OrderManagement;

import java.util.Random;

public class Location {
    protected int posX;
    protected int posY;

    public Location() {
        int maxRadiusInMilliseconds = 7200;
        this.posX = new Random().nextInt(2 * maxRadiusInMilliseconds) - maxRadiusInMilliseconds;    //random in range
        int maxY = (int)Math.sqrt(Math.pow(maxRadiusInMilliseconds,2)-Math.pow(this.posX,2));
        this.posY = new Random().nextInt(2 * maxY ) - maxY ;    //random in range
    }

    public Location(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public int getPosX(){
        return this.posX;
    }
    public int getPosY(){
        return this.posY;
    }

    public boolean equals(Location position) {
        return (posX == position.posX && posY == position.posY);
    }

    public int distanceTo(Location p) {
        double x0 = posX;
        double x1 = p.posX;
        double y0 = posY;
        double y1 = p.posY;

        return (int)(Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2)));
    }

    public Location positionOnRoute(Location p, int distancePassed) {
        double distanceTotal = distanceTo(p);

        double x0 = posX;
        double x1 = p.posX;
        double y0 = posY;
        double y1 = p.posY;

        int x2 = (int)(   ((double)distancePassed/distanceTotal) * (x1 - x0) + x0);
        int y2 = (int)(   ((double)distancePassed/distanceTotal) * (y1 - y0) + y0);
        return new Location(x2, y2);

    }
}