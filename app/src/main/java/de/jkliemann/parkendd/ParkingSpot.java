package de.jkliemann.parkendd;

import android.location.Location;
import android.net.Uri;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by jkliemann on 10.12.14.
 */
public class ParkingSpot {
    private final String name;
    private final String category;
    private final String state;
    private final String city;
    private final double lat;
    private final double lon;
    private final int count;
    private final int free;

    public static enum byNAME implements Comparator<ParkingSpot>{
        INSTANCE;
        @Override
        public int compare(ParkingSpot p1, ParkingSpot p2){
            return p1.name().compareTo(p2.name());
        }
    }

    public static enum byDISTANCE implements Comparator<ParkingSpot>{
        INSTANCE;
        @Override
        public int compare(ParkingSpot p1, ParkingSpot p2){
            GlobalSettings gs = GlobalSettings.getGlobalSettings();
            Location currentLocation = gs.getLastKnownLocation();
            Double d1 = new Double(Util.getDistance(p1.location(), currentLocation));
            Double d2 = new Double(Util.getDistance(p2.location(), currentLocation));
            return d1.compareTo(d2);
        }
    }

    public ParkingSpot(String name, String category, String state, String city, int count, int free, double lat, double lon){
        this.name = name;
        this.category = category;
        this.state = state;
        this.city = city;
        this.count = count;
        this.free = free;
        this.lat = lat;
        this.lon = lon;
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
        String location;
        if(lat != 0 && lon != 0){
            location = "geo:0,0?q="+Double.toString(lat)+","+Double.toString(lon)+"("+name+")";
        }else{
            location = "geo:0,0?q="+city+" " + name;
        }
        return Uri.parse(location);
    }

    public Location location(){
        if(lat != 0 && lon != 0) {
            Location location = new Location("gps");
            location.setLatitude(lat);
            location.setLongitude(lon);
            return location;
        }else{
            return null;
        }
    }


    static public ParkingSpot[] getSortedArray(ParkingSpot[] slotList, Comparator<ParkingSpot> comparator) {
        ParkingSpot[] sorted = slotList.clone();
        Arrays.sort(sorted, comparator);
        return sorted;

    }
}
