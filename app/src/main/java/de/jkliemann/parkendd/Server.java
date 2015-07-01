package de.jkliemann.parkendd;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jkliemann on 05.01.15.
 */

public class Server extends AsyncTask<String, Void, ArrayList<City>> {

    private static final String MAIL = "mail";
    private static final String CITIES = "cities";
    private static final String VERSION = "api_version";
    private String mail;
    private ArrayList<City> citylist;
    private String version;
    private int error = 0;

    private ArrayList<City> parseJSon(String data){
        ArrayList<City> cities = new ArrayList<>();
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        try{
            JSONObject global = new JSONObject(data);
            try{
                version = global.getString(VERSION);
                String[] vs = version.split("\\.");
                gs.setAPI(Integer.parseInt(vs[0]), Integer.parseInt(vs[1]));
            }catch (JSONException e){
                e.printStackTrace();
                gs.setAPI(0, 0);
            }
            if(gs.getAPI_V_MAJOR() == 0 && gs.getAPI_V_MINOR() == 0) {
                mail = global.getString(MAIL);
                JSONArray citystrings = global.getJSONArray(CITIES);
                for (int i = 0; i < citystrings.length(); i++) {
                    cities.add(new City(citystrings.getString(i), citystrings.getString(i)));
                }
            }
            if(gs.getAPI_V_MAJOR() == 1){
                JSONObject citystrings = global.getJSONObject(CITIES);
                Iterator<String> city_ids = citystrings.keys();
                while(city_ids.hasNext()){
                    String id = city_ids.next();
                    try{
                        cities.add(new City(id, citystrings.getString(id)));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
            error = 1;
        }
        return cities;
    }

    protected ArrayList<City> doInBackground(String... urlstring){
        String meta = "";
        try {
            URL url = new URL(urlstring[0]);
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
            }catch (IOException e) {
                e.printStackTrace();
                error = 2;
            }
            if(connection != null){
                try {
                    BufferedReader br;
                    InputStream in = connection.getInputStream();
                    br = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        meta = meta + line;
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                    error = 2;
                }
                connection.disconnect();
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
            error = 3;
        }
        citylist = parseJSon(meta);
        return citylist;
    }

    protected void onPostExecute(ArrayList<City> cities) {
    }
}
