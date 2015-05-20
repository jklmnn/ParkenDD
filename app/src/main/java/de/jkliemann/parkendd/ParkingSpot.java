package de.jkliemann.parkendd;

import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by jkliemann on 10.12.14.
 */
public class ParkingSpot implements Parcelable{
    private String name;
    private String category;
    private String state;
    private String city;
    private double lat;
    private double lon;
    private int count;
    private int free;
    private Boolean forecast;

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

   public static enum byFREE implements Comparator<ParkingSpot>{
       INSTANCE;
       @Override
       public int compare(ParkingSpot p1, ParkingSpot p2){
           Float f1 = 1 - ((float)p1.free()) / ((float)p1.count());
           Float f2 = 1 - ((float)p2.free()) / ((float)p2.count());
           return f1.compareTo(f2);
       }
   }

    public ParkingSpot(String name, String category, String state, String city, int count, int free, double lat, double lon, Boolean forecast){
        this.name = name;
        this.category = category;
        this.state = state;
        this.city = city;
        this.count = count;
        this.free = free;
        this.lat = lat;
        this.lon = lon;
        this.forecast = forecast;
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

    public Boolean forecast(){
        return forecast;
    }


    static public ParkingSpot[] getSortedArray(ParkingSpot[] slotList, Comparator<ParkingSpot> comparator) {
        ParkingSpot[] sorted = slotList.clone();
        Arrays.sort(sorted, comparator);
        return sorted;

    }

    //parcelable implementation

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel out, int flags){
        out.writeString(name);
        out.writeString(category);
        out.writeString(state);
        out.writeString(city);
        out.writeDouble(lat);
        out.writeDouble(lon);
        out.writeInt(count);
        out.writeInt(free);
        out.writeByte((byte) (forecast ? 1:0));
    }

    public static final Parcelable.Creator<ParkingSpot> CREATOR = new Parcelable.Creator<ParkingSpot>() {
        public ParkingSpot createFromParcel(Parcel in) {
            return new ParkingSpot(in);
        }

        public ParkingSpot[] newArray(int size) {
            return new ParkingSpot[size];
        }
    };

    private ParkingSpot(Parcel in){
        name = in.readString();
        category = in.readString();
        state = in.readString();
        city = in.readString();
        lat = in.readDouble();
        lon = in.readDouble();
        count = in.readInt();
        free = in.readInt();
        forecast = in.readByte() != 0;
    }
}
