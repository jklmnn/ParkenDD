package de.jkliemann.parkendd;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;

import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.jkliemann.parkendd.Model.City;
import de.jkliemann.parkendd.Utilities.Util;

/**
 * Created by jkliemann on 10.08.15.
 */
public class ParkenDD extends Application {
    Tracker piwik;
    private City currentCity;
    private Map<Integer, City> cmap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Boolean locationEnabled;
    private Location providedLocation;
    private Boolean autoCity;
    private int API_V_MAJOR;
    private int API_V_MINOR;
    private static Context context;

    public Boolean initLocation(){
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

    @Override
    public void onCreate(){
        cmap = new HashMap<>();
        initLocation();
        setLocation(null);
        ParkenDD.context = getApplicationContext();
        super.onCreate();
    }

    public synchronized Tracker getTracker(){
        if(piwik != null){
            return piwik;
        }
        try{
            piwik = Piwik.getInstance(this).newTracker("https://piwik.jkliemann.de", 3);
        }catch (MalformedURLException e){
            e.printStackTrace();
            return null;
        }
        return piwik;
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

    public void setCurrentCity(City city) {
        this.currentCity = this.getCityByName(city.name());
        autoCity = false;
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
