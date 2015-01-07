package de.jkliemann.parkendd;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by jkliemann on 05.01.15.
 */

public class Server extends AsyncTask<Context, Void, String[]> {

    private static final String MAIL = "mail";
    private static final String CITIES = "cities";
    private String mail;
    private String[] citylist;
    private Context context;

    private String[] parseJSon(String data){
        ArrayList<String> cities = new ArrayList<String>();
        try{
            JSONObject global = new JSONObject(data);
            mail = global.getString(MAIL);
            JSONArray citystrings = global.getJSONArray(CITIES);
            for(int i = 0; i < citystrings.length(); i++){
                cities.add(citystrings.getString(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return cities.toArray(new String[cities.size()]);
    }

    protected String[] doInBackground(Context... context){
        this.context = context[0];
        String urlstring = PreferenceManager.getDefaultSharedPreferences(this.context).getString("fetch_url", this.context.getString(R.string.default_fetch_url));
        String meta = "";
        try {
            URL url = new URL(urlstring);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader br = null;
            InputStream in = connection.getInputStream();
            try{
                br = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while((line = br.readLine()) != null){
                    meta = meta + line;
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        citylist = parseJSon(meta);
        return citylist;
    }

    protected void onPostExecute(String[] cities){
        GlobalSettings.getGlobalSettings().setMail(mail);
        GlobalSettings.getGlobalSettings().setCitylist(citylist);
    }
}
