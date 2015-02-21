package de.jkliemann.parkendd;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;

/**
 * Created by jkliemann on 07.01.15.
 */
public class GlobalSettings {

    private static GlobalSettings mInstance = null;
    private String[] citylist;
    private String mail;
    private Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String locationProvider;

    private GlobalSettings(){
        citylist = null;
        mail = "";
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

    public Location getLastKnownLocation(){
        return locationManager.getLastKnownLocation(locationProvider);
    }


    public String[] getCitylist(){
        return citylist;
    }

    public String getMail(){
        return mail;
    }

    public void setCitylist(String[] citylist){
        this.citylist = citylist;
    }

    public void setMail(String mail){
        this.mail = mail;
    }
}
