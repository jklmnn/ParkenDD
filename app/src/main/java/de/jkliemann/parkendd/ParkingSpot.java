package de.jkliemann.parkendd;

import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

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
    private String id;
    private String type;
    private String address;
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
            if(p1.location() == null && p2.location() == null){
                return 0;
            }else if(p1.location() == null && p2.location() != null){
                return 1;
            }else if(p2.location() == null && p1.location() != null){
                return -1;
            }
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
           if(p1.state().equals("nodata") && p2.state().equals("closed")){
               return -1;
           }else if(p1.state().equals("closed") && p2.state().equals("nodata")){
               return 1;
           }
           if(p1.state().equals("nodata") && !p2.state().equals("closed") && !p2.state().equals("nodata")){
               return 1;
           }
           if(p2.state().equals("nodata") && !p1.state().equals("closed") && !p1.state().equals("nodata")){
               return -1;
           }
           Double b1 = new Double(p1.count() - p1.free());
           Double b2 = new Double(p2.count() - p2.free());
           return b1.compareTo(b2);
       }
   }

    public static enum byEUKLID implements Comparator<ParkingSpot>{
        INSTANCE;
        @Override
        public int compare(ParkingSpot p1, ParkingSpot p2){
            try{
                return p1.rating().compareTo(p2.rating());
            }catch (NullPointerException e) {
                return 0;
            }
        }
    }

    public ParkingSpot(String name, String state, String city, String id, int count, int free, double lat, double lon, Boolean forecast){
        this.name = name;
        this.state = state;
        this.city = city;
        this.id = id;
        this.count = count;
        this.free = free;
        this.lat = lat;
        this.lon = lon;
        this.forecast = forecast;
        this.type = "";
    }

    private Double rating(){
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        Location currentLocation = gs.getLastKnownLocation();
        Double d;
        try{
            d = new Double(Util.getDistance(this.location(), currentLocation));
        }catch (NullPointerException e){
            e.printStackTrace();
            d = Double.MAX_VALUE;
        }
        Double b = (1 - ((double) this.free()) / ((double) this.count()));
        Double e = Math.sqrt(Math.pow(d, 2) + Math.pow(b, 2)) * (1 / (Math.pow(2 * (1 - b), 2) + 1));
        //<Parkhaus Mitte>
        if(this.id().equals("dresdenparkhausmitte") && d < 2000 && b < 0.4){
            e = (double)-1;
        }
        //</Parkhaus Mitte>
        if(this.state().equals("closed") || this.free() == 0){
            e = Double.POSITIVE_INFINITY;
        }
        return e;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public void setState(String state) throws NullPointerException{
        this.state = state;
    }

    public void setCount(int count) throws NullPointerException{
        this.count = count;
    }

    public void setFree(int free) throws NullPointerException{
        this.free = free;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setAddress(String address){
        this.address = address;
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

    public String id(){
        return id;
    }

    public String type(){
        return type;
    }

    public String address(){
        return address;
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
            location = "geo:0,0?q="+Double.toString(lat)+","+Double.toString(lon);
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


    static public ParkingSpot[] getSortedArray(ParkingSpot[] slotList, Comparator<ParkingSpot> comparator){
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
        out.writeString(id);
        out.writeString(type);
        out.writeString(address);
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
        id = in.readString();
        type = in.readString();
        address = in.readString();
        lat = in.readDouble();
        lon = in.readDouble();
        count = in.readInt();
        free = in.readInt();
        forecast = in.readByte() != 0;
    }
}
