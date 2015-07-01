package de.jkliemann.parkendd;

import android.location.Location;

import java.util.ArrayList;

import javax.security.auth.callback.Callback;

/**
 * Created by jkliemann on 01.07.15.
 */
public interface ServerInterface {
    void onMetaFinished(ArrayList<City> cities);
    void onFetchFinished(City city);
    void onNominatimFinished(Location location);
}
