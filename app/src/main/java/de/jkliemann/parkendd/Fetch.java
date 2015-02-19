package de.jkliemann.parkendd;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

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
 * Created by jkliemann on 10.12.14.
 */
public class Fetch extends AsyncTask<String, Void, ArrayList<ParkingSpot>> {

    private static final String NAME = "name";
    private static final String LOTS = "lots";
    private static final String COUNT = "count";
    private static final String FREE = "free";
    private static final String STATE = "state";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static String CITY = "";
    private ListView spotView = null;
    private Context context = null;
    private RelativeLayout popup = null;
    private int error = 0;

    public void setUi(ListView spotView, Context context, RelativeLayout popup){
        this.spotView = spotView;
        this.context = context;
        this.popup = popup;
        CITY = PreferenceManager.getDefaultSharedPreferences(context).getString("city", context.getString(R.string.default_city));
        popup.setVisibility(View.VISIBLE);
    }

    private static Uri geoUriFromCoord(String lat, String lon, String city, String label){
        String location;
        if(!lat.equals("null") && !lon.equals("null")){
            location = "geo:0,0?q="+lat+","+lon+"("+label+")";
        }else{
            location = "geo:0,0?q="+city+" " + label;
        }
        return Uri.parse(location);
    }

    private ArrayList<ParkingSpot> parseJSon(String json){
        ArrayList<ParkingSpot> spots = new ArrayList<ParkingSpot>();
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
                    String lat = lot.getString(LAT);
                    String lon = lot.getString(LON);
                    String city = CITY;
                    if(count.length() < 1 || count.equals("null")){
                        count = "0";
                    }
                    if(free.length() < 1 || free.equals("null")){
                        free = "0";
                    }
                    spots.add(new ParkingSpot(name, category, state, city, Integer.parseInt(count), Integer.parseInt(free), geoUriFromCoord(lat, lon, city, name)));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
            error = 2;
        }
        return spots;
    }

   protected ArrayList<ParkingSpot> doInBackground(String... ct){
       String json = "";
       ArrayList<ParkingSpot> spots = null;
       String address = PreferenceManager.getDefaultSharedPreferences(context).getString("fetch_url", context.getString(R.string.default_fetch_url));
       address = address + "?city=" + PreferenceManager.getDefaultSharedPreferences(context).getString("city", context.getString(R.string.default_city));
       try {
           URL url = new URL(address);
           HttpURLConnection cn = null;
           try {
               if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("ignore_cert", false)) {
                   cn = Error.getUnsecureConnection(url);
               }else {
                   cn = (HttpURLConnection) url.openConnection();
               }
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
                   spots = parseJSon(json);
               } catch (IOException e) {
                   e.printStackTrace();
                   error = 2;
               } finally {
                   if (br != null) {
                       try {
                           br.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                           error = 2;
                       }
                   }
               }
           }
       }catch(MalformedURLException e){
           e.printStackTrace();
           error = 4;
       }
       return spots;
   }

    protected void onPostExecute(ArrayList<ParkingSpot> spots){
        if(context != null && spotView != null && spots != null) {
            final ParkingSpot[] spotArray = spots.toArray(new ParkingSpot[spots.size()]);
            SlotListAdapter adapter = new SlotListAdapter(context, spotArray);
            spotView.setAdapter(adapter);
            spotView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Uri geoUri = spotArray[position].geoUri();
                    try {
                        Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
                        context.startActivity(mapCall);
                    }catch(ActivityNotFoundException e){
                        e.printStackTrace();
                        Error.showLongErrorToast(context, context.getString(R.string.intent_error));
                    }
                }
            });
        }
        popup.setVisibility(View.GONE);
        switch (error){
            case 2:
                Error.showLongErrorToast(context, context.getString(R.string.invalid_error));
                break;
            case 1:
                Error.showLongErrorToast(context, context.getString(R.string.network_error));
                break;
            case 3:
                Error.showLongErrorToast(context, context.getString(R.string.connection_error));
                break;
            case 4:
                Error.showLongErrorToast(context, context.getString(R.string.url_error));
                break;
            default:
                break;
        }
    }
}
