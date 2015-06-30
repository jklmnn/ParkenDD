package de.jkliemann.parkendd;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

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

public class Server extends AsyncTask<Context, Void, ArrayList<City>> {

    private static final String MAIL = "mail";
    private static final String CITIES = "cities";
    private static final String VERSION = "api_version";
    private String mail;
    private ArrayList<City> citylist;
    private Context context;
    private String version;
    private int error = 0;
    private ListView spotView;
    private ProgressBar popup;

    public void setUi(ListView spotView, Context context, ProgressBar popup){
        this.spotView = spotView;
        this.context = context;
        this.popup = popup;
        popup.setVisibility(View.VISIBLE);
    }

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
        popup.setProgress(15);
        return cities;
    }

    protected ArrayList<City> doInBackground(Context... context){
        this.context = context[0];
        String urlstring = PreferenceManager.getDefaultSharedPreferences(this.context).getString("fetch_url", this.context.getString(R.string.default_fetch_url));
        String meta = "";
        try {
            URL url = new URL(urlstring);
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                popup.setProgress(5);
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
                    try {
                        int len = connection.getContentLength();
                        double part;
                        while ((line = br.readLine()) != null) {
                            part = (double)line.length() / (double)len;
                            meta = meta + line;
                            popup.setProgress(popup.getProgress() + (int)(part * 90));
                        }
                    }catch (NullPointerException e){
                        while ((line = br.readLine()) != null) {
                            meta = meta + line;
                        }
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                    error = 2;
                }
                connection.disconnect();
            }
            popup.setProgress(95);
        }catch (MalformedURLException e){
            e.printStackTrace();
            error = 3;
        }
        citylist = parseJSon(meta);
        return citylist;
    }

    protected void onPostExecute(ArrayList<City> cities){
        GlobalSettings.getGlobalSettings().setMail(mail);
        GlobalSettings.getGlobalSettings().setCitylist(citylist);
        switch (error){
            case 1:
                Error.showLongErrorToast(context, context.getString(R.string.invalid_error));
                return;
            case 2:
                Error.showLongErrorToast(context, context.getString(R.string.invalid_error));
                return;
            case 3:
                Error.showLongErrorToast(context, context.getString(R.string.url_error));
                return;
            default:
                break;
        }
        Fetch ff = new Fetch();
        popup.setProgress(100);
        ff.setUi(spotView, this.context, popup);
        ff.execute(PreferenceManager.getDefaultSharedPreferences(context).getString("fetch_url", context.getString(R.string.default_fetch_url)));
    }
}
