package de.jkliemann.parkendd;

import android.location.Location;

import java.util.Formatter;


/**
 * Created by jkliemann on 21.02.15.
 */
public class Util {

    public static double getDistance(Location src, Location dst){
        return src.distanceTo(dst);
    }

    public static String getViewDistance(double dist){
        String vdist = "";
        if(dist < 1000){
            vdist = "ca. " + Integer.toString((int)Math.round(dist / 10) * 10) + "m";
        }else if( dist < 10000){
            Formatter fmt = new Formatter();
            fmt.format("%.1f", dist/1000);
            vdist = "ca. " + fmt.toString() + "km";
        }else{
            vdist = "ca. " + Integer.toString((int)Math.round(dist) / 1000) + "km";
        }
        return vdist;
    }

}
