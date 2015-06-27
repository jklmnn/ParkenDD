package de.jkliemann.parkendd;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jkliemann on 07.01.15.
 */
public class GlobalSettings {

    private static GlobalSettings mInstance = null;
    private String[] citylist;
    private Map<String, String> idmap;
    private String mail;
    private Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String locationProvider;
    private int API_V_MAJOR;
    private int API_V_MINOR;

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

    public Location getLastKnownLocation() {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("use_location", true)){
            return locationManager.getLastKnownLocation(locationProvider);
        }else {
            return null;
        }
    }


    public String[] getCitylist(){
        if(API_V_MAJOR == 0) {
            return citylist;
        }else if(API_V_MAJOR == 1){
            ArrayList<String> idlist = new ArrayList<>();
            for(String id : citylist){
                idlist.add(this.getCityById(id));
            }
            return idlist.toArray(new String[idlist.size()]);
        }
        return null;
    }

    public String getMail(){
        return mail;
    }

    public int getAPI_V_MAJOR(){
        return API_V_MAJOR;
    }

    public int getAPI_V_MINOR(){
        return API_V_MINOR;
    }

    public String getCityById(String id){
        return this.idmap.get(id);
    }

    public void setCitylist(String[] citylist){
        this.citylist = citylist;
    }

    public void setIdMap(Map<String, String> idmap){
        this.idmap = idmap;
    }

    public void setMail(String mail){
        this.mail = mail;
    }

    public void setAPI(int maj, int min){
        this.API_V_MAJOR = maj;
        this.API_V_MINOR = min;
    }
}
