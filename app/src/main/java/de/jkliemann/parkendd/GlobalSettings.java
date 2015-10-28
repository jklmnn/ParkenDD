package de.jkliemann.parkendd;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;

import java.util.ArrayList;

/**
 * Created by jkliemann on 07.01.15.
 */
public class GlobalSettings {

    private static GlobalSettings mInstance = null;
    private ArrayList<City> citylist;
    private Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private int API_V_MAJOR;
    private int API_V_MINOR;
    private Location providedLocation = null;
    private Boolean locationEnabled = false;

    private GlobalSettings(){
        citylist = null;
        locationManager = null;
        locationListener = null;
    }

    public static GlobalSettings getGlobalSettings(){
        if(mInstance == null){
            mInstance = new GlobalSettings();
        }
        return mInstance;
    }

    public Boolean locationEnabled(){
        return locationEnabled;
    }

    public Boolean initLocation(Context context){
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        try {
            locationManager.requestLocationUpdates(getProvider(), (long) 60000, (float) 50, locationListener, Looper.getMainLooper());
            locationEnabled = true;
            return true;
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            locationEnabled = false;
            return false;
        }
    }

    private String getProvider(){
        if(locationManager.isProviderEnabled("gps")){
            return "gps";
        }
        if(locationManager.isProviderEnabled("network")){
            return "network";
        }
        return null;
    }

    public void setLocation(Location loc){
        if(loc == null){
            try {
                providedLocation = locationManager.getLastKnownLocation(getProvider());
            }catch (IllegalArgumentException e){
                e.printStackTrace();
                providedLocation = null;
            }
        }else{
            providedLocation = loc;
        }
    }

    public Location getLastKnownLocation() {
        return providedLocation;
    }


    public ArrayList<City> getCitylist(){
        return getActiveCities();
    }

    public int getAPI_V_MAJOR(){
        return API_V_MAJOR;
    }

    public int getAPI_V_MINOR(){
        return API_V_MINOR;
    }

    public City getCityById(String id){
        for(City city: getActiveCities()){
            if(city.id().equals(id)){
                return city;
            }
        }
        return null;
    }

    private City getClosestCity(){
        if(providedLocation == null){
            return getCityByName("Dresden");
        }
        double distance = Double.MAX_VALUE;
        City closestCity = null;
        for(City city : getActiveCities()){
            double d;
            try {
                d = Util.getDistance(providedLocation, city.location());
            }catch (NullPointerException e){
                d = Double.MAX_VALUE;
            }
            if(d < distance){
                distance = d;
                closestCity = city;
            }
        }
        return closestCity;
    }

    public City getCityByName(String name){
        if(name.equals(context.getString(R.string.default_city))){
            return this.getClosestCity();
        }
        for(City city : this.getActiveCities()){
            if(city.name().equals(name)){
                return city;
            }
        }
        return null;
    }

    public void setCitylist(Object citylist){
        if(citylist instanceof ArrayList){
            this.citylist = (ArrayList)citylist;
        }
    }

    public void setAPI(int maj, int min){
        this.API_V_MAJOR = maj;
        this.API_V_MINOR = min;
    }

    private ArrayList<City> getActiveCities(){
        if(PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("active_support", true)) {
            ArrayList<City> activeList = new ArrayList<>();
            for(City city : this.citylist){
                if(city.active_support()){
                    activeList.add(city);
                }
            }
            return activeList;
        }else{
            return this.citylist;
        }
    }
}
