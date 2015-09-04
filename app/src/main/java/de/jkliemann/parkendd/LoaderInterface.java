package de.jkliemann.parkendd;

/**
 * Created by jkliemann on 23.08.15.
 */
public interface LoaderInterface {
    void onExceptionThrown(Exception e);
    void onLoaderFinished(String data[], Loader instance);
    void onProgressUpdated();
}
