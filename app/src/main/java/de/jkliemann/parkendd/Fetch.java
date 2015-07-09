package de.jkliemann.parkendd;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * Created by jkliemann on 10.12.14.
 */
public class Fetch extends AsyncTask<String, Void, City> {

    private static final String NAME = "name";
    private static final String LOTS = "lots";
    private static final String TOTAL = "total";
    private static final String FREE = "free";
    private static final String STATE = "state";
    private static final String COORDS = "coords";
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private static final String FORECAST = "forecast";
    private static final String LOT_TYPE = "lot_type";
    private static final String DATA_SOURCE = "data_source";
    private static final String LAST_DOWNLOADED = "last_downloaded";
    private static final String LAST_UPDATED = "last_updated";
    private static final String ID = "id";
    private static final String ADDRESS = "address";
    private static final String REGION = "region";
    private City CITY;
    public static final int PROGRESS = 7;

    private final FetchInterface fetchFinished;

    public Fetch(FetchInterface f){
        this.fetchFinished = f;
    }

    private void fetchAPI_1_0(String fetch_url){
        String data = "";
        String address;
        if(!fetch_url.substring(fetch_url.length() - 1).equals("/")){
            address = fetch_url + "/";
        }else {
            address = fetch_url;
        }
        try{
            URL url = null;
            HttpURLConnection connection;
            try{
                url = new URL(address + URLEncoder.encode(this.CITY.id(), "UTF-8"));
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }catch (MalformedURLException e){
                e.printStackTrace();
                return;
            }
            try{
                connection = (HttpURLConnection)url.openConnection();
                publishProgress();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    data = data + line;
                }
                publishProgress();
                br.close();
                connection.disconnect();
                publishProgress();
            }catch (IOException e){
                e.printStackTrace();
                return;
            }
            ArrayList<ParkingSpot> spotlist = new ArrayList<>();
            String last_downloaded = "";
            String last_updated = "";
            String data_source = "";
            try{
                JSONObject global = new JSONObject(data);
                last_downloaded = global.getString(LAST_DOWNLOADED);
                last_updated = global.getString(LAST_UPDATED);
                data_source = global.getString(DATA_SOURCE);
                JSONArray spotarray = global.getJSONArray(LOTS);
                publishProgress();
                for(int i = 0; i < spotarray.length(); i++){
                    try {
                        JSONObject lot = spotarray.getJSONObject(i);
                        String name = lot.getString(NAME);
                        String state = lot.getString(STATE);
                        String city = CITY.name();
                        String id = lot.getString(ID);
                        String type = "";
                        String adr = "";
                        String region = "";
                        try{
                            type = lot.getString(LOT_TYPE);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        try{
                            adr = lot.getString(ADDRESS);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        try {
                            region = lot.getString(REGION);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        int total = lot.getInt(TOTAL);
                        int free = lot.getInt(FREE);
                        double lat, lon;
                        try {
                            JSONObject coord = lot.getJSONObject(COORDS);
                            lat = coord.getDouble(LAT);
                            lon = coord.getDouble(LNG);
                        } catch (JSONException e) {
                            lat = 0;
                            lon = 0;
                        }
                        Boolean forecast = lot.getBoolean(FORECAST);
                        ParkingSpot spot = new ParkingSpot(name, state, city, id, total, free, lat, lon, forecast);
                        spot.setType(type);
                        spot.setAddress(adr);
                        spot.setCategory(region);
                        spotlist.add(spot);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    publishProgress();
                }
            }catch (JSONException e){
                e.printStackTrace();
                return;
            }
            CITY.setSpots(spotlist);
            CITY.setData_source(data_source);
            CITY.setLast_downloaded(last_downloaded);
            CITY.setLast_updated(last_updated);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    protected void onProgressUpdate(Void... v){
        fetchFinished.updateProgress();
    }


    protected City doInBackground(String... ct){
        CITY = GlobalSettings.getGlobalSettings().getCityByName(ct[1]);
        String fetch_url = ct[0];
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        publishProgress();
        if(gs.getAPI_V_MAJOR() == 1 && gs.getAPI_V_MINOR() == 0){
            try {
                fetchAPI_1_0(fetch_url);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        publishProgress();
        return CITY;
    }

    protected void onPostExecute(City c) {
        fetchFinished.onFetchFinished(c);
    }
}
