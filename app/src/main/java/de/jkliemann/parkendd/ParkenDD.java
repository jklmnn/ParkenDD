package de.jkliemann.parkendd;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jkliemann on 10.08.15.
 */
public class ParkenDD extends Application {
    private City currentCity;
    private Map<Integer, City> cmap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Boolean locationEnabled = false;
    private Location providedLocation;
    private Boolean autoCity;
    private int API_V_MAJOR;
    private int API_V_MINOR;
    private static Context context;

    public Boolean initLocation(Activity c){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
            if(ContextCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(c, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }else {
                locationManager.requestLocationUpdates(getProvider(), (long) 60000, (float) 50, locationListener, Looper.getMainLooper());
                locationEnabled = true;
            }
        }catch (IllegalArgumentException e) {
            e.printStackTrace();
            locationEnabled = false;
        }
        return locationEnabled;
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

    public void setLocation(Location loc, Activity c){
        if(loc == null){
            try {
                if(ContextCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    providedLocation = locationManager.getLastKnownLocation(getProvider());
                }else{
                    providedLocation = null;
                }
            }catch (IllegalArgumentException e){
                e.printStackTrace();
                providedLocation = null;
            }
        }else{
            providedLocation = loc;
        }
    }

    @Override
    public void onCreate(){
        cmap = new HashMap<>();
        ParkenDD.context = getApplicationContext();
        super.onCreate();
    }

    public void addCityPair(int id, City city){
        cmap.put(id, city);
    }

    public void updateCities(ArrayList<City> citylist){
        for(City city : citylist){
            if(!cmap.containsValue(city)){
                cmap.put(cmap.size(), city);
            }
        }
    }

    private City getCityByName(String name){
        for(City city : cmap.values()){
            if(city.name().equals(name)){
                return city;
            }
        }
        return null;
    }

    public City getClosestCity(){
        if(providedLocation == null){
            return getCityByName("Dresden");
        }
        double distance = Double.MAX_VALUE;
        City closestCity = null;
        for(City city : getActiveCities(new ArrayList<>(cmap.values()))){
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


    public City getCityById(int id){
        if(id == 0){
            return getClosestCity();
        }else{
            return cmap.get(id);
        }
    }

    public void setCurrentCity(int id){
        this.currentCity = this.getCityById(id);
        if(id == 0){
            autoCity = true;
        }else{
            autoCity = false;
        }
    }

    public City currentCity(){
        if(currentCity == null){
            setCurrentCity(0);
            autoCity = true;
        }
        return currentCity;
    }

    public Location location(){
        return providedLocation;
    }

    public Boolean locationEnabled(){
        return locationEnabled;
    }

    public Boolean autoCity(){
        return autoCity;
    }

    public static Context applicationContext(){
        return ParkenDD.context;
    }

    public ArrayList<City> getActiveCities(ArrayList<City> citylist){
        if(PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("active_support", true)) {
            ArrayList<City> activeList = new ArrayList<>();
            for(City city : citylist){
                if(city.active_support()){
                    activeList.add(city);
                }
            }
            return activeList;
        }else{
            return citylist;
        }
    }

    public int getAPI_V_MAJOR(){
        return API_V_MAJOR;
    }

    public int getAPI_V_MINOR(){
        return API_V_MINOR;
    }

    public void setAPI(int maj, int min){
        this.API_V_MAJOR = maj;
        this.API_V_MINOR = min;
    }
}
