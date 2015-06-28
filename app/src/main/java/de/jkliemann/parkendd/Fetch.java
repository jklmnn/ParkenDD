package de.jkliemann.parkendd;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
    private static final String FORECAST = "forecast";
    private static City CITY = null;
    private ListView spotView = null;
    private Context context = null;
    private RelativeLayout popup = null;
    private int error = 0;

    public void setUi(ListView spotView, Context context, RelativeLayout popup){
        this.spotView = spotView;
        this.context = context;
        this.popup = popup;
        CITY = GlobalSettings.getGlobalSettings().getCityByName(PreferenceManager.getDefaultSharedPreferences(context).getString("city", context.getString(R.string.default_city)));
        popup.setVisibility(View.VISIBLE);
    }

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
                    spots.add(new ParkingSpot(name, category, state, city, "", Integer.parseInt(count), Integer.parseInt(free), lat, lon, forecast));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
            error = 2;
        }
        return spots;
    }

    private ArrayList<ParkingSpot> fetchOldAPI(){
        String json = "";
        ArrayList<ParkingSpot> spots = null;
        String address = PreferenceManager.getDefaultSharedPreferences(context).getString("fetch_url", context.getString(R.string.default_fetch_url));
        try {
            URL url = new URL(address + URLEncoder.encode(this.CITY.name(), "UTF-8"));
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
                    spots = parseOldJSon(json);
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
        }catch(MalformedURLException e) {
            e.printStackTrace();
            error = 4;
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
            error = 4;
        }
        return spots;
    }

   protected ArrayList<ParkingSpot> doInBackground(String... ct){
       GlobalSettings gs = GlobalSettings.getGlobalSettings();
       if(gs.getAPI_V_MAJOR() == 0 && gs.getAPI_V_MINOR() == 0){
           return fetchOldAPI();
       }
       return null;
   }

    protected void onPostExecute(ArrayList<ParkingSpot> spots){
        if(context != null && spotView != null && spots != null) {
            String sortOptions[] = this.context.getResources().getStringArray(R.array.setting_sort_options);
            String sortPreference = PreferenceManager.getDefaultSharedPreferences(this.context).getString("sorting", sortOptions[0]);
            Boolean hide_closed = PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("hide_closed", true);
            Boolean hide_nodata = PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("hide_nodata", false);
            Boolean hide_full = PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("hide_full", true);
            final ParkingSpot[] spotArray;
            ParkingSpot[] preArray;
            ArrayList<ParkingSpot> cachelist = new ArrayList<>();
            for(ParkingSpot spot : spots){
                if(hide_closed && spot.state().equals("closed")){
                    cachelist.add(spot);
                }
                if(hide_nodata && spot.state().equals("nodata")){
                    cachelist.add(spot);
                }
                if(hide_full && spot.free() == 0 && !spot.state().equals("nodata") && !spot.state().equals("closed")){
                    cachelist.add(spot);
                }
            }
            for (ParkingSpot spot : cachelist){
                spots.remove(spot);
            }
            if(sortPreference.equals(sortOptions[0])){
                try{
                    preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byEUKLID.INSTANCE);
                }catch (NullPointerException e){
                    e.printStackTrace();
                    preArray = spots.toArray(new ParkingSpot[spots.size()]);
                }
            }else if(sortPreference.equals(sortOptions[1])) {
                try {
                    preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byNAME.INSTANCE);
                }catch (NullPointerException e){
                    e.printStackTrace();
                    preArray = spots.toArray(new ParkingSpot[spots.size()]);
                }
            }else if(sortPreference.equals(sortOptions[2])) {
                try {
                    preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byDISTANCE.INSTANCE);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    preArray = spots.toArray(new ParkingSpot[spots.size()]);
                }
            }else if(sortPreference.equals(sortOptions[3])) {
                try {
                    preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byFREE.INSTANCE);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    preArray = spots.toArray(new ParkingSpot[spots.size()]);
                }
            }else{
                preArray = spots.toArray(new ParkingSpot[spots.size()]);
            }
            spotArray = preArray;
            SlotListAdapter adapter = new SlotListAdapter(context, spotArray);
            spotView.setAdapter(adapter);
            spotView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Intent details = new Intent(context, DetailsActivity.class);
                        details.putExtra("spot", spotArray[position]);
                        context.startActivity(details);

                    } catch (ActivityNotFoundException e) {
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
