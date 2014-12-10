package de.jkliemann.parkendd;

/**
 * Created by jkliemann on 10.12.14.
 */
public class ParkingSpot {
    private String name;
    private String category;
    private int count;
    private int free;

    public ParkingSpot(String n, String c, int cn, int fr){
        name = n;
        category = c;
        count = cn;
        free = fr;
    }

    public String name(){
        return name;
    }

    public String category(){
        return category;
    }

    public int count(){
        return count;
    }

    public int free(){
        return free;
    }
}
