package de.jkliemann.parkendd;

import android.location.Location;

/**
 * Created by jkliemann on 02.07.15.
 */
public interface NominatimInterface extends AsyncInterface {
    void onNominatimFinished(Location location);
}
