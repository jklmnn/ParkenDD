package de.jkliemann.parkendd;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jkliemann on 05.01.15.
 */

public class Server extends AsyncTask<Context, Void, String[]> {

    private static final String MAIL = "mail";
    private static final String CITIES = "cities";
    private static final String VERSION = "api_version";
    private String mail;
    private String[] citylist;
    private Context context;
    private String version;
    private int error = 0;

    private String[] parseJSon(String data){
        ArrayList<String> cities = new ArrayList<String>();
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
                    cities.add(citystrings.getString(i));
                }
            }
            if(gs.getAPI_V_MAJOR() == 1){
                JSONObject citystrings = global.getJSONObject(CITIES);
                Iterator<String> city_ids = citystrings.keys();
                Map<String, String> idmap = new HashMap<>();
                while(city_ids.hasNext()){
                    String id = city_ids.next();
                    try{
                        cities.add(id);
                        idmap.put(id, citystrings.getString(id));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                gs.setIdMap(idmap);
            }
        }catch (JSONException e){
            e.printStackTrace();
            error = 1;
        }
        return cities.toArray(new String[cities.size()]);
    }

    protected String[] doInBackground(Context... context){
        this.context = context[0];
        String urlstring = PreferenceManager.getDefaultSharedPreferences(this.context).getString("fetch_url", this.context.getString(R.string.default_fetch_url));
        String meta = "";
        try {
            URL url = new URL(urlstring);
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
            }catch (IOException e) {
                e.printStackTrace();
                error = 2;
            }
            if(connection != null){
                try {
                    BufferedReader br = null;
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
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
            error = 3;
        }
        citylist = parseJSon(meta);
        return citylist;
    }

    protected void onPostExecute(String[] cities){
        GlobalSettings.getGlobalSettings().setMail(mail);
        GlobalSettings.getGlobalSettings().setCitylist(citylist);
        switch (error){
            case 1:
                Error.showLongErrorToast(context, context.getString(R.string.invalid_error));
                break;
            case 2:
                Error.showLongErrorToast(context, context.getString(R.string.invalid_error));
                break;
            case 3:
                Error.showLongErrorToast(context, context.getString(R.string.url_error));
                break;
            default:
                break;
        }
    }
}
