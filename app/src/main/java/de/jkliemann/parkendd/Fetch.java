package de.jkliemann.parkendd;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
    private static final String COUNT = "count";
    private static final String TOTAL = "total";
    private static final String FREE = "free";
    private static final String STATE = "state";
    private static final String COORDS = "coords";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String LNG = "lng";
    private static final String FORECAST = "forecast";
    private static final String DATA_SOURCE = "data_source";
    private static final String LAST_DOWNLOADED = "last_downloaded";
    private static final String LAST_UPDATED = "last_updated";
    private static final String ID = "id";
    private City CITY;
    private int error = 0;

    private ArrayList<ParkingSpot> parseOldJSon(String json){
        ArrayList<ParkingSpot> spots = new ArrayList<>();
        try{
            JSONArray global = new JSONArray(json);
            for(int i = 0; i < global.length(); i++){
                JSONObject catg = global.getJSONObject(i);
                String category = catg.getString(NAME);
                JSONArray lots = catg.getJSONArray(LOTS);
                for(int j = 0; j < lots.length(); j++){
                    JSONObject lot = lots.getJSONObject(j);
                    String name = lot.getString(NAME);
                    String count = lot.getString(COUNT);
                    String free = lot.getString(FREE);
                    String state = lot.getString(STATE);
                    if(state.equals("few") || state.equals("many") || state.equals("full")){
                        state = "open";
                    }
                    double lat, lon;
                    Boolean forecast = lot.getBoolean(FORECAST);
                    try {
                        lat = lot.getDouble(LAT);
                        lon = lot.getDouble(LON);
                    }catch (JSONException e){
                        lat = 0;
                        lon = 0;
                    }
                    String city = CITY.name();
                    if(count.length() < 1 || count.equals("null")){
                        count = "0";
                    }
                    if(free.length() < 1 || free.equals("null")){
                        free = "0";
                    }
                    spots.add(new ParkingSpot(name, state, city, "", Integer.parseInt(count), Integer.parseInt(free), lat, lon, forecast));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
            error = 2;
        }
        return spots;
    }

    private ArrayList<ParkingSpot> fetchOldAPI(String fetch_url){
        String json = "";
        ArrayList<ParkingSpot> spots = null;
        String address = fetch_url;
        try {
            URL url = new URL(address + URLEncoder.encode(this.CITY.id(), "UTF-8"));
            HttpURLConnection cn = null;
            try {
                cn = (HttpURLConnection) url.openConnection();
            }catch (IOException e){
                e.printStackTrace();
                error = 3;
                cn = null;
            }
            if(cn != null) {
                BufferedReader br = null;
                try{
                    InputStream in = cn.getInputStream();
                    br = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        json = json + line + "\n";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    error = 1;
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            error = 1;
                        }
                    }
                }
            }
            cn.disconnect();
            spots = parseOldJSon(json);
        }catch(MalformedURLException e) {
            e.printStackTrace();
            error = 4;
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
            error = 4;
        }
        return spots;
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
                error = 4;
                return;
            }
            try{
                connection = (HttpURLConnection)url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    data = data + line;
                }
                br.close();
                connection.disconnect();
            }catch (IOException e){
                e.printStackTrace();
                error = 3;
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
                for(int i = 0; i < spotarray.length(); i++){
                    try {
                        JSONObject lot = spotarray.getJSONObject(i);
                        String name = lot.getString(NAME);
                        String state = lot.getString(STATE);
                        String city = CITY.name();
                        String id = CITY.id();
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
                        spotlist.add(spot);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
                error = 2;
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

    protected City doInBackground(String... ct){
        CITY = GlobalSettings.getGlobalSettings().getCityByName(ct[1]);
        String fetch_url = ct[0];
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        if(gs.getAPI_V_MAJOR() == 0 && gs.getAPI_V_MINOR() == 0){
            CITY.setSpots(fetchOldAPI(fetch_url));
        }
        if(gs.getAPI_V_MAJOR() == 1 && gs.getAPI_V_MINOR() == 0){
            fetchAPI_1_0(fetch_url);
        }
        return CITY;
    }

    protected void onPostExecute(City c) {
    }
}
