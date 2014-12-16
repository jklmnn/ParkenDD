package de.jkliemann.parkendd;

import android.net.Uri;

/**
 * Created by jkliemann on 10.12.14.
 */
public class ParkingSpot {
    private final String name;
    private final String category;
    private final String state;
    private final Uri geoUri;
    private final int count;
    private final int free;

    public ParkingSpot(String name, String category, String state, int count, int free, Uri geoUri){
        this.name = name;
        this.category = category;
        this.state = state;
        this.count = count;
        this.free = free;
        this.geoUri = geoUri;
    }

    public String name(){
        return name;
    }

    public String category(){
        return category;
    }

    public String state(){
        return state;
    }

    public int count(){
        return count;
    }

    public int free(){
        return free;
    }

    public Uri geoUri(){
        return geoUri;
    }
}
