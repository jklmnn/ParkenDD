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
    private String locationProvider;
    private int API_V_MAJOR;
    private int API_V_MINOR;
    private Location providedLocation = null;

    private GlobalSettings(){
        citylist = null;
        locationManager = null;
        locationListener = null;
        locationProvider = "network";
    }

    public static GlobalSettings getGlobalSettings(){
        if(mInstance == null){
            mInstance = new GlobalSettings();
        }
        return mInstance;
    }

    public void initLocation(Context context){
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
        if(!locationManager.isProviderEnabled(locationProvider)){
            locationProvider = "gps";
        }
        locationManager.requestLocationUpdates(locationProvider, (long) 60000, (float) 50, locationListener, Looper.getMainLooper());
    }

    public void setLocation(Location loc){
        if(loc == null){
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("use_location", true)){
                providedLocation = locationManager.getLastKnownLocation(locationProvider);
            }else {
                return;
            }
        }else{
            providedLocation = loc;
        }
    }

    public Location getLastKnownLocation() {
        return providedLocation;
    }


    public ArrayList<City> getCitylist(){
        return citylist;
    }

    public int getAPI_V_MAJOR(){
        return API_V_MAJOR;
    }

    public int getAPI_V_MINOR(){
        return API_V_MINOR;
    }

    public City getCityById(String id){
        for(City city: this.citylist){
            if(city.id().equals(id)){
                return city;
            }
        }
        return null;
    }

    public City getCityByName(String name){
        for(City city : this.citylist){
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
}
