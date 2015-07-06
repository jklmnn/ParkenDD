package de.jkliemann.parkendd;

import java.util.ArrayList;

/**
 * Created by jkliemann on 01.07.15.
 */
public interface ServerInterface extends AsyncInterface{
    void onMetaFinished(ArrayList<City> cities);
}
