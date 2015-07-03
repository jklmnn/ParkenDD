package de.jkliemann.parkendd;

import java.util.Date;
import java.util.Map;

/**
 * Created by jkliemann on 03.07.15.
 */
public interface FetchForecastInterface extends AsyncInterface{
    void onForecastFinished(Map<ParkingSpot, Map<Date, Integer>> forecastMap);
}
