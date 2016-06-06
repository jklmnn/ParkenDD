package de.jkliemann.parkendd;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

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
        super.onCreate();
        setLocation(null);
    }

    synchronized Tracker getTracker(){
        if(piwik != null){
            return piwik;
        }
        try{
            piwik = Piwik.getInstance(this).newTracker("https://jkliemann.de/analytics/piwik.php", 3);
        }catch (MalformedURLException e){
            e.printStackTrace();
            return null;
        }
        return piwik;
    }

    public void addCityPair(int id, City city){
        cmap.put(id, city);
    }

    public City getCityById(int id){
        if(id == 0){
            return GlobalSettings.getGlobalSettings().getClosestCity();
        }else{
            return cmap.get(id);
        }
    }

    public void setCurrentCity(int id){
        this.currentCity = this.getCityById(id);
        if(id == 0){
            autoCity = true;
        }
    }

    public City currentCity(){
        if(currentCity == null){
            setCurrentCity(0);
            autoCity = true;
        }else{
            autoCity = false;
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
}
