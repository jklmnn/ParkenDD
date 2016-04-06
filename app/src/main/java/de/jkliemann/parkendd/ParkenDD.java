package de.jkliemann.parkendd;

import android.app.Application;

import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;

import java.net.MalformedURLException;

/**
 * Created by jkliemann on 10.08.15.
 */
public class ParkenDD extends Application {
    Tracker piwik;

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
}
