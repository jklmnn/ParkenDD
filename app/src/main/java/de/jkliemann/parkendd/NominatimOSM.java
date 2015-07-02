package de.jkliemann.parkendd;


import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jkliemann on 30.06.15.
 */
public class NominatimOSM extends AsyncTask<Object, Void, Location> {

    private static final String host = "https://nominatim.openstreetmap.org";
    private static final String format = "json";

    public static final int PROGRESS = 3;

    private final NominatimInterface osmFinished;

    public NominatimOSM(NominatimInterface osmf){

        osmFinished = osmf;
    }

    protected void onProgressUpdate(Void... v){
        osmFinished.updateProgress();
    }


    protected Location doInBackground(Object... objs){
        Uri geouri = null;
        String data = "";
        if(objs[0] instanceof Uri){
            geouri = (Uri)objs[0];
        }
        try{
            String query = geouri.getEncodedQuery();
            URL url = null;
            try{
                url = new URL(host + "/search?" + query + "&format=" + format + "&addressdetails=1");
            }catch (MalformedURLException e){
                e.printStackTrace();
            }
            HttpsURLConnection connection;
            try{
                connection = (HttpsURLConnection)url.openConnection();
                publishProgress();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while((line = br.readLine()) != null){
                    data = data + line;
                }
                Log.i("OSM JSON", data);
                br.close();
                connection.disconnect();
                publishProgress();
            }catch (IOException e){
                e.printStackTrace();
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }
        Location loc = null;
        try{
            JSONArray osm = new JSONArray(data);
            if(osm.length() > 0){
                JSONObject jsondata = osm.getJSONObject(0);
                double lat = jsondata.getDouble("lat");
                double lon = jsondata.getDouble("lon");
                loc = new Location("gps");
                loc.setLatitude(lat);
                loc.setLongitude(lon);
                JSONObject address = jsondata.getJSONObject("address");
                String name = address.getString("road") + " " + address.getString("house_number") + "\n" + address.getString("postcode") + " " + address.getString("city");
                Bundle extra = new Bundle();
                extra.putString("detail", name);
                loc.setExtras(extra);
            }
            publishProgress();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return loc;
    }

    protected void onPostExecute(Location loc){
        osmFinished.onNominatimFinished(loc);
    }
}
