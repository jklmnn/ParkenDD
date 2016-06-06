package de.jkliemann.parkendd;

import android.app.Application;

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

    @Override
    public void onCreate(){
        cmap = new HashMap<>();
        super.onCreate();
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
    }

    public City currentCity(){
        if(currentCity == null){
            setCurrentCity(0);
        }
        return currentCity;
    }
}
