package de.jkliemann.parkendd;

/**
 * Created by jkliemann on 02.07.15.
 */
public interface FetchInterface extends AsyncInterface{
    void onFetchFinished(City city);
}
