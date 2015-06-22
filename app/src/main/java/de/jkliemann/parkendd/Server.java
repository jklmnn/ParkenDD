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

/**
 * Created by jkliemann on 05.01.15.
 */

public class Server extends AsyncTask<Context, Void, String[]> {

    private static final String MAIL = "mail";
    private static final String CITIES = "cities";
    private static final String VERSION_MAJOR = "version_major";
    private static final String VERSION_MINOR = "version_minor";
    private String mail;
    private String[] citylist;
    private Context context;
    private int major = 0;
    private int minor = 0;
    private int error = 0;

    private String[] parseJSon(String data){
        ArrayList<String> cities = new ArrayList<String>();
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        try{
            JSONObject global = new JSONObject(data);
            mail = global.getString(MAIL);
            try{
                major = global.getInt(VERSION_MAJOR);
                minor = global.getInt(VERSION_MINOR);
                gs.setAPI(major, minor);
            }catch (JSONException e){
                e.printStackTrace();
                gs.setAPI(0, 0);
            }
            JSONArray citystrings = global.getJSONArray(CITIES);
            for(int i = 0; i < citystrings.length(); i++){
                cities.add(citystrings.getString(i));
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
                if(PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("ignore_cert", false)){
                    connection = Util.getUnsecureConnection(url);
                }else {
                    connection = (HttpURLConnection) url.openConnection();
                }
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
